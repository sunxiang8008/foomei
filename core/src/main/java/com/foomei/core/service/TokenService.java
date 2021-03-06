package com.foomei.core.service;

import com.foomei.common.service.impl.JpaServiceImpl;
import com.foomei.common.time.DateUtil;
import com.foomei.core.dao.jpa.TokenDao;
import com.foomei.core.entity.Token;
import com.foomei.core.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 令牌管理业务类.
 *
 * @author walker
 */
@Service
public class TokenService extends JpaServiceImpl<Token, String> {

  @Autowired
  private TokenDao tokenDao;

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void disable(Long userId) {
    tokenDao.disable(userId);
    LOGGER.info("disable entity {}, userId is {}", Token.class.getName(), userId);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public String apply(Long userId, String userAgent, String remark) {
    disable(userId);

    Token token = new Token();
    token.setUser(new User(userId));
    token.setUserAgent(userAgent);
    token.setRemark(remark);
    token.setEnableTime(new Date());
    token.setExpireTime(DateUtil.addDays(new Date(), Token.DEFAULT_EXPIRE));
    token.setStatus(Token.STATUS_ENABLE);
    token.setCreateTime(new Date());

    save(token);

    return token.getId();
  }

  public boolean activate(Long userId) {
    List<Token> tokens = tokenDao.findByEnable(userId);
    return !tokens.isEmpty();
  }

}
