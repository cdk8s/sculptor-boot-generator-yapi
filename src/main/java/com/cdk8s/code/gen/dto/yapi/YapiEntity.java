package com.cdk8s.code.gen.dto.yapi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
@ToString(callSuper = true)
public class YapiEntity implements Serializable {

	private static final long serialVersionUID = 7975415790497139511L;

	private int index;
	private String name;
	private String desc;
	private int add_time;
	private int up_time;
	private List<ListBean> list;

	@NoArgsConstructor
	@Setter
	@Getter
	@ToString(callSuper = true)
	public static class ListBean {

		private QueryPathBean query_path;
		private int edit_uid;
		private String status;
		private String type;
		private boolean req_body_is_json_schema;
		private boolean res_body_is_json_schema;
		private boolean api_opened;
		private int index;
		private int _id;
		private String method;
		private int catid;
		private String title;
		private String path;
		private int project_id;
		private String res_body_type;
		private String desc;
		private String markdown;
		private String req_body_type;
		private String res_body;
		private String req_body_other;
		private int uid;
		private int add_time;
		private int up_time;
		private int __v;
		private List<?> tag;
		private List<?> req_params;
		private List<?> req_query;
		private List<ReqHeadersBean> req_headers;
		private List<?> req_body_form;

		@NoArgsConstructor
		@Setter
		@Getter
		@ToString(callSuper = true)
		public static class QueryPathBean {

			private String path;
			private List<?> params;
		}

		@NoArgsConstructor
		@Setter
		@Getter
		@ToString(callSuper = true)
		public static class ReqHeadersBean {

			private String required;
			private String _id;
			private String name;
			private String value;
			private String example;
			private String desc;
		}
	}
}
