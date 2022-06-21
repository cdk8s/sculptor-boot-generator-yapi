package com.cdk8s.code.gen.dto.yapi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;


@Setter
@Getter
@ToString(callSuper = true)
@NoArgsConstructor
public class YapiJsonTemplateEntity implements Serializable {

	private List<YapiEntity> content;

}
