package com.foomei.common.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

/**
 * 统一定义id的entity基类.
 * 
 * 基类统一定义id的属性名称、数据类型、列名映射及生成策略.
 * 子类可重载getId()函数重定义id的列名映射和生成策略.
 * 
 * @author walker
 */
@MappedSuperclass
public abstract class UuidEntity implements Serializable {
    
	private static final long serialVersionUID = 2570752361703229119L;
	public static final String PROP_ID = "id";
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
	protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof UuidEntity)) {
            return false;
        }
        UuidEntity another = (UuidEntity) o;
        return new EqualsBuilder().append(this.getId(), another.getId()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(id).hashCode();
    }

    public String toString() {
        return "(id=" + getId() + ")";
    }
	
}
