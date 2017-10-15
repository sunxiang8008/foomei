package com.foomei.core.entity;

import com.foomei.common.entity.CreateRecord;
import com.foomei.common.entity.UuidEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * 附件
 *
 * @author walker
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "Core_Annex")
@SuppressWarnings("serial")
public class Annex extends UuidEntity implements CreateRecord {

  public static final String PROP_OBJECT_ID = "objectId";
  public static final String PROP_OBJECT_TYPE = "objectType";
  public static final String PROP_PATH = "path";
  public static final String PROP_NAME = "name";
  public static final String PROP_TYPE = "type";
  public static final String PROP_CREATE_TIME = "createTime";
  public static final String PROP_CREATOR = "creator";

  private String objectId;//对象ID
  private String objectType;//对象类型
  private String path;//存储路径
  private String name;//文件名称
  private String type;//文件类型
  private Date createTime;//创建时间
  private Long creator;//创建人

  public boolean isCreated() {
    return id != null;
  }

}
