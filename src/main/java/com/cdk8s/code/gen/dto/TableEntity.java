package com.cdk8s.code.gen.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString(callSuper = true)
public class TableEntity {

	private String tableName;

	private String comments;

	private ColumnEntity pk;

	private List<ColumnEntity> columns;

	private String upperClassName;

	private String lowerClassName;


	private Boolean boolMySQL8;
}
