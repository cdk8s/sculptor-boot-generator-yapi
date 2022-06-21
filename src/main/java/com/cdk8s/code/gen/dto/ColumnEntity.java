package com.cdk8s.code.gen.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(callSuper = true)
public class ColumnEntity {

	private String columnName;


	private String columnDefault;


	private Integer maxValue;


	private Boolean boolIsEnum = false;


	private Boolean boolIsNullable = false;


	private Integer characterMaximumLength;


	private String dataType;


	private String comment;


	private String shortComment;


	private String upperAttrName;


	private String lowerAttrName;


	private String attrType;


	private String extra;
}
