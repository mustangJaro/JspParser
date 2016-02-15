package com.webapp.parse;

import java.util.List;

import com.webapp.parse.JspParser.TAG_TYPE;

public class Element {
	private String qName;
	private TAG_TYPE tagType;
	private List<Attribute> attrs;
	private boolean openedAndClosed = false;
	public String getqName() {
		return qName;
	}
	public void setqName(String qName) {
		this.qName = qName;
	}
	public TAG_TYPE getTagType() {
		return tagType;
	}
	public void setTagType(TAG_TYPE tagType) {
		this.tagType = tagType;
	}
	public List<Attribute> getAttrs() {
		return attrs;
	}
	public void setAttrs(List<Attribute> attrs) {
		this.attrs = attrs;
	}
	public boolean isOpenedAndClosed() {
		return openedAndClosed;
	}
	public void setOpenedAndClosed(boolean openedAndClosed) {
		this.openedAndClosed = openedAndClosed;
	}
	public String toString(){
		String result = "";
		result += "<" + qName + " ";
		if(attrs != null){
			for(Attribute attr : attrs){
				result += attr.toString();
			}
		}
		if(openedAndClosed)
			result += "/";
		result += ">"; 
		return result;
	}

}
