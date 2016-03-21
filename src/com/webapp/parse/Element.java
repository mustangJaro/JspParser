package com.webapp.parse;

import java.util.List;

import com.webapp.parse.TAG_TYPE;

public class Element {
	private String qName;
	private TAG_TYPE tagType;
	private List<Attribute> attrs;
	private boolean openedAndClosed = false;
	private int lineNumber = 0;
	
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
		result += "<";
		if(tagType.equals(TAG_TYPE.COMMENT))
			result += "!--";
		result += qName;
		if(attrs != null){
			for(Attribute attr : attrs){
				result += " " + attr.toString();
			}
		}
		if(tagType.equals(TAG_TYPE.COMMENT))
			result += "--";
		else if(openedAndClosed)
			result += "/";
		result += ">"; 
		return result;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

}
