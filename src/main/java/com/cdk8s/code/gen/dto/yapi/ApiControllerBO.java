package com.cdk8s.code.gen.dto.yapi;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(callSuper = true)
public class ApiControllerBO {


	private String apiPath;
	private String apiClassName;
	private String apiComment;
	private String apiMethodName;
	private String requestParamClassName;
	private String responseDTOClassName;
}
