package com.webapp.parse;

/**
 * Basic interface that {@link JspParser} uses when finding
 * elements, values, and whitespace
 *
 */
public interface ParserHandler {
	
	public void startFile(String filename);
	public void startElement(Element el);
	public void endElement(Element el);
	public void elementValue(String s);
	public void showWhitespace(String s);
	public void endFile(String filename);
}
