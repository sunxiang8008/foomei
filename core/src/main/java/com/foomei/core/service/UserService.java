package com.foomei.core.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.foomei.common.web.ThreadContext;
import com.google.common.base.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foomei.common.persistence.Hibernates;
import com.foomei.common.security.DigestUtil;
import com.foomei.common.service.impl.JpaServiceImpl;
import com.foomei.common.text.EncodeUtil;
import com.foomei.core.dao.jpa.UserDao;
import com.foomei.core.entity.Role;
import com.foomei.core.entity.User;
import com.foomei.core.entity.UserGroup;

/**
 * 用户管理业务类.
 * 
 * @author walker
 */
@Service
@Transactional(readOnly = true)
public class UserService extends JpaServiceImpl<UserDao, User, Long>{
	public static final String HASH_ALGORITHM = "SHA-1";
	public static final int HASH_INTERATIONS = 1024;
	private static final int SALT_SIZE = 8;

	@Autowired
	private UserDao userDao;

	public User getByLoginName(String loginName) {
		return userDao.findByLoginName(loginName);
	}
	
	public User getByWeixin(String openId) {
		return userDao.findByOpenId(openId);
	}
	
	/**
	 * 按名称查询用户, 并对用户的延迟加载关联进行初始化.
	 */
	public User getByLoginNameInitialized(String name) {
		User user = userDao.findByLoginName(name);
		if (user != null) {
			Hibernates.initLazyProperty(user.getRoleList());
			Hibernates.initLazyProperty(user.getGroupList());
		}
		return user;
	}

	/**
	 * 获取全部用户对象，并在返回前完成LazyLoad属性的初始化。
	 */
	public List<User> getAllInitialized() {
		List<User> result = userDao.findAll();
		for (User user : result) {
			Hibernates.initLazyProperty(user.getRoleList());
			Hibernates.initLazyProperty(user.getGroupList());
		}
		return result;
	}

	/**
	 * 获取当前用户数量.
	 */
	public Long getCount() {
		return userDao.count();
	}

	/**
	 * 在保存用户时,发送用户修改通知消息, 由消息接收者异步进行较为耗时的通知邮件发送.
	 *
	 * 如果企图修改超级用户,取出当前操作员用户,打印其信息然后抛出异常.
	 *
	 */
	@Transactional(readOnly = false)
	public User save(User user) {
		if (isSupervisor(user)) {
			logger.warn("操作员{}尝试修改超级管理员用户", ThreadContext.getUserName());
			throw new ServiceException("不能修改超级管理员用户");
		}

		// 设定安全的密码，生成随机的salt并经过1024次 sha-1 hash
		if (StringUtils.isNotBlank(user.getPlainPassword())) {
			entryptPassword(user);
		}

		user = super.save(user);
		logger.info("save entity: {}", user);
		return user;
	}
	
	@Transactional(readOnly = false)
    public void start(Long id) {
        User user = get(id);
        user.setStatus(User.STATUS_ACTIVE);
        save(user);
    }

	@Transactional(readOnly = false)
	public void delete(Long id) {
//		userDao.delete(id);
		//停用
		User user = get(id);
		user.setStatus(User.STATUS_TERMINATED);
		save(user);
	}
	
	@Transactional(readOnly = false)
	public User bindingWeixin(Long id, String openId) {
		User user = get(id);
		user.setOpenId(openId);
		save(user);
		return user;
	}
	
	@Transactional(readOnly = false)
	public User unbindingWeixin(Long id) {
		User user = get(id);
		user.setOpenId(null);
		save(user);
		return user;
	}
	
	public Page<User> getPageByRole(final String searchKey, final Long roleId, Pageable page) {
        return userDao.findAll(new Specification<User>() {
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                
                if (StringUtils.isNotEmpty(searchKey)) {
                    Predicate p1 = cb.like(root.get(User.PROP_LOGIN_NAME).as(String.class), "%" + StringUtils.trimToEmpty(searchKey) + "%");
                    Predicate p2 = cb.like(root.get(User.PROP_NAME).as(String.class), "%" + StringUtils.trimToEmpty(searchKey) + "%");
                    Predicate p3 = cb.like(root.get(User.PROP_MOBILE).as(String.class), "%"+StringUtils.trimToEmpty(searchKey)+"%");
                    Predicate p4 = cb.like(root.get(User.PROP_EMAIL).as(String.class), "%" + StringUtils.trimToEmpty(searchKey) + "%");
                    predicates.add(cb.or(p1, p2, p3, p4));
                }
                
                if (roleId != null) {
                    predicates.add(cb.equal(root.join(User.PROP_ROLE_LIST).get(Role.PROP_ID).as(Long.class), roleId));
                }
                
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, page);
    }
    
    public Page<User> getPageByGroup(final String searchKey, final Long groupId, Pageable page) {
        return userDao.findAll(new Specification<User>() {
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                
                if (StringUtils.isNotEmpty(searchKey)) {
                    Predicate p1 = cb.like(root.get(User.PROP_LOGIN_NAME).as(String.class), "%" + StringUtils.trimToEmpty(searchKey) + "%");
                    Predicate p2 = cb.like(root.get(User.PROP_NAME).as(String.class), "%" + StringUtils.trimToEmpty(searchKey) + "%");
                    Predicate p3 = cb.like(root.get(User.PROP_MOBILE).as(String.class), "%"+StringUtils.trimToEmpty(searchKey)+"%");
                    Predicate p4 = cb.like(root.get(User.PROP_EMAIL).as(String.class), "%" + StringUtils.trimToEmpty(searchKey) + "%");
                    predicates.add(cb.or(p1, p2, p3, p4));
                }
                
                if (groupId != null) {
                    predicates.add(cb.equal(root.join(User.PROP_GROUP_LIST).get(UserGroup.PROP_ID).as(Long.class), groupId));
                }
                
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, page);
    }
	
	public boolean existLoginName(Long id, String loginName) {
		User user = getByLoginName(loginName);
		if(user == null || (id != null && id.equals(user.getId()))) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean existOpenId(String openId) {
		User user = getByWeixin(openId);
		return user != null;
	}
	
	public boolean existGroup(final Long groupId) {
		Specification<User> conditions = new Specification<User>() {
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.join(User.PROP_GROUP_LIST).get(UserGroup.PROP_ID).as(Long.class), groupId);
			}
		};
		List<User> users = userDao.findAll(conditions);
		return !users.isEmpty();
	}
	
	public boolean checkPassword(final Long id, final String password) {
		User user = get(id);
		if(user != null) {
			String hashPassword = EncodeUtil.encodeHex(DigestUtil.sha1(password.getBytes(Charsets.UTF_8), EncodeUtil.decodeHex(user.getSalt()), HASH_INTERATIONS));
			return StringUtils.equalsIgnoreCase(hashPassword, user.getPassword());
		}
		
		return false;
	}

	@Transactional(readOnly = false)
	public void changePassword(Long id, String password) {
		User user = get(id);
		user.setPlainPassword(password);
		entryptPassword(user);

		userDao.save(user);
		logger.info("change password for: {}", user.getName());
	}
	
	@Transactional(readOnly = false)
	public void changePassword(String username, String password) {
		User user = getByLoginName(username);
		user.setPlainPassword(password);
		entryptPassword(user);

		userDao.save(user);
		logger.info("change password for: {}", username);
		// TODO:发送邮件提醒用户
	}

	@Transactional(readOnly = false)
	public void loginSuccess(String loginName, String ip) {
		User user = getByLoginName(loginName);
		user.setLastLoginTime(new Date());
		user.setLastLoginIp(ip);
		user.addLoginCount();

		userDao.save(user);
		logger.info("login success for: {}", loginName);
	}

	/**
	 * 设定安全的密码，生成随机的salt并经过1024次 sha-1 hash
	 */
	private void entryptPassword(User user) {
		byte[] salt = DigestUtil.generateSalt(SALT_SIZE);
		user.setSalt(EncodeUtil.encodeHex(salt));

		byte[] hashPassword = DigestUtil.sha1(user.getPlainPassword().getBytes(Charsets.UTF_8), salt, HASH_INTERATIONS);
		user.setPassword(EncodeUtil.encodeHex(hashPassword));
	}

	/**
	 * 判断是否超级管理员.
	 */
	private boolean isSupervisor(User user) {
		return (user.getId() != null && user.getId() == 1L);
	}

}
