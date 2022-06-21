package com.cdk8s.code.gen.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@NoArgsConstructor
@Setter
@Getter
@ToString(callSuper = true)
public class ResponseBaseEntity implements Serializable {

	private int code;
	private boolean isSuccess;
	private String msg;
	private long timestamp;
	private Object data;
}
