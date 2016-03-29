package com.webapp.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Given a JSP file and a {@link ParserHandler}, this guy
 * will run through the JSP file notifying the handler
 * of various events
 *
 */
public class JspParser {
	private BufferedReader br;
	private int lineNumber = 1;
	
	/**
	 * This method is the one to be used to run through a file
	 * 
	 * @param inputFile		This file should be in a JSP format
	 * @param handler		This is used for callbacks on events
	 * @throws IOException
	 */
	public void parse(File inputFile, ParserHandler handler) throws IOException{
		
		br = new BufferedReader(new FileReader(inputFile)); 
		handler.startFile(inputFile.getName());
		lineNumber = 1;

		int c;
		boolean inJavaScript = false;
		StringBuilder elValue = new StringBuilder();
		
		while ((c = br.read()) != -1) {
			char ch = (char) c;
			
			switch(ch){
			case '<':
				br.mark(2);
				char next = (char) br.read();
				br.reset();
				if(next == '/'){
					if(elValue.toString().trim().length() > 0){
						handler.elementValue(elValue.toString());
						elValue = new StringBuilder();
					}else if(elValue.toString().length() > 0){
						handler.showWhitespace(elValue.toString());
						elValue = new StringBuilder();
					}
					Element el = parseCloseTag();
					el.setLineNumber(lineNumber);
					handler.endElement(el);
					
					inJavaScript = false;
				}else if(!inJavaScript){
					if(elValue.toString().trim().length() > 0){
						handler.elementValue(elValue.toString());
						elValue = new StringBuilder();
					}else if(elValue.toString().length() > 0){
						handler.showWhitespace(elValue.toString());
						elValue = new StringBuilder();
					}
					Element el = parseOpenTag();
					el.setLineNumber(lineNumber);					
					handler.startElement(el);
					
					if(el.getTagType().equals(TAG_TYPE.SCRIPT))
						inJavaScript = true;
					
					if(el.isOpenedAndClosed() || !(el.getTagType().equals(TAG_TYPE.STANDARD) || el.getTagType().equals(TAG_TYPE.SCRIPT))){
						handler.endElement(el);
						inJavaScript = false;
					}
				}else if(inJavaScript){
					if(ch == '\n')
						lineNumber++;
					elValue.append(ch);
				}
				break;
			default:
				if(ch == '\n')
					lineNumber++;
				elValue.append(ch);
				break;
			}
		}
		handler.endFile(inputFile.getName());
		
		br.close();		
	}
	
	/**
	 * Parses through the open tag call and will determine the {@link TAG_TYPE}
	 * and populate {@link Element} appropriately
	 * 
	 * @return	
	 * @throws IOException
	 */
	private Element parseOpenTag() throws IOException{
		Element el = new Element();
		boolean reachedEndOfTag = false;
		boolean inQuotes = false;
		StringBuilder qName = new StringBuilder();
		char ch = (char) br.read();
		TAG_TYPE tagType = TAG_TYPE.STANDARD;
		if(ch == '%'){
			tagType = TAG_TYPE.JSP;
			//@
			br.read();
			//(space)
			br.read();
			//first char
			ch = (char) br.read();
		}else if(ch == '!'){
			br.mark(2);
			ch = (char) br.read();
			if(ch == '-'){
				return parseCommentTag();
			}else{
				tagType = TAG_TYPE.DOCTYPE;
				br.reset();
			}
		}else if(ch == 's'){
			br.mark(6);
			if(((char) br.read()) == 'c' && ((char) br.read()) == 'r' && ((char) br.read()) == 'i' && ((char) br.read()) == 'p' && ((char) br.read()) == 't'){
				tagType = TAG_TYPE.SCRIPT;
			}
			br.reset();
		}
		
		
		while(!reachedEndOfTag){
			if(ch == '>'){
				reachedEndOfTag = true;
			}else{
				switch(ch){
				case ' ':
					el = parseAttributes(tagType);
					reachedEndOfTag = true;
					continue;
				case '/':
					if(!inQuotes)
						el.setOpenedAndClosed(true);
					break;
				case '"':
					inQuotes = !inQuotes;
					break;
				default:
					if(ch == '\n'){
						lineNumber++;
						el = parseAttributes(tagType);
						reachedEndOfTag = true;
					}else
						qName.append(ch);
					break;
				}
				ch = (char) br.read();
			}
		}
		el.setqName(qName.toString());
		if(qName.toString().equalsIgnoreCase("meta"))
			el.setTagType(TAG_TYPE.META);
		else
			el.setTagType(tagType);
		return el;
	}
	
	/**
	 * Parse through attributes of a given tag type
	 * 
	 * @param tagType
	 * @return
	 * @throws IOException
	 */
	private Element parseAttributes(TAG_TYPE tagType) throws IOException{
		Element el = new Element();
		boolean reachedEndOfTag = false;
		boolean inQuotes = false;
		StringBuilder value = new StringBuilder();
		StringBuilder name = new StringBuilder();
		Attribute attr = new Attribute();
		List<Attribute> attrs = new ArrayList<Attribute>();
		char ch = (char) br.read();
		while(!reachedEndOfTag){
			if(!inQuotes && ch == '>'){
				reachedEndOfTag = true;
				if(tagType.equals(TAG_TYPE.DOCTYPE) && name.toString().trim().length() > 0){
					attr.setName(name.toString());
					value = new StringBuilder();
					name = new StringBuilder();
					attrs.add(attr);
				}
					
			}else if(ch == '%' && tagType.equals(TAG_TYPE.JSP)){
				br.read();
				reachedEndOfTag = true;			
			}else{
				if(!inQuotes){
					switch(ch){
					case '=':
						attr.setName(name.toString());
						break;
					case ' ':
					case '\r':
						if(attr.getName() != null && attr.getName().length() > 0 
							&& value.toString().trim().length() > 0){
							attr.setValue(value.toString());
							value = new StringBuilder();
							name = new StringBuilder();
							attrs.add(attr);
							attr = new Attribute();
						}else if(tagType.equals(TAG_TYPE.DOCTYPE)){
							attr.setName(name.toString());
							value = new StringBuilder();
							name = new StringBuilder();
							attrs.add(attr);
							attr = new Attribute();
						}
						break;
					case '/':
						el.setOpenedAndClosed(true);
						break;
					case '"':
						inQuotes = !inQuotes;
						break;
					default:
						if(ch == '\n')
							lineNumber++;
						if(attr.getName() == null || attr.getName().length() == 0)
							name.append(ch);
						else
							value.append(ch);
						break;
					}
				}else if(ch == '"'){
					inQuotes = !inQuotes;
					if(attr.getName() != null && attr.getName().length() > 0){
						attr.setValue(value.toString());
						value = new StringBuilder();
						name = new StringBuilder();
						attrs.add(attr);
						attr = new Attribute();
					}
				}else if(tagType.equals(TAG_TYPE.DOCTYPE)){
					name.append(ch);
				}else
					value.append(ch);
				
				ch = (char) br.read();
			}
		}
		if(attr.getName() != null && attr.getName().length() > 0 && !tagType.equals(TAG_TYPE.DOCTYPE)){
			attr.setValue(value.toString());
			value = new StringBuilder();
			name = new StringBuilder();
			attrs.add(attr);
		}
		el.setAttrs(attrs);
		return el;
	}
	
	/**
	 * Pretty simple method to look through the close tag of an element
	 * 
	 * @return
	 * @throws IOException
	 */
	private Element parseCloseTag() throws IOException{
		Element el = new Element();

		boolean reachedEndOfTag = false;
		StringBuilder qName = new StringBuilder();
		br.read();
		char ch = (char) br.read();
		
		
		while(!reachedEndOfTag){
			if(ch == '>'){
				reachedEndOfTag = true;
			}else{
				if(ch == '\n')
					lineNumber++;
				qName.append(ch);
				ch = (char) br.read();
			}
		}
		el.setqName(qName.toString());
		el.setTagType(TAG_TYPE.STANDARD);
		return el;
	}
	
	/**
	 * Again, another simple one to look through a comment tag.  This will
	 * populate the qName as the comment value
	 * @return
	 * @throws IOException
	 */
	private Element parseCommentTag() throws IOException{
		Element el = new Element();

		boolean reachedEndOfTag = false;
		int consecutiveDashCount = 0;
		StringBuilder comment = new StringBuilder();
		br.read();
		char ch = (char) br.read();
		
		while(!reachedEndOfTag){
			if(ch == '>' && consecutiveDashCount > 1){
				reachedEndOfTag = true;
				break;
			}else if(ch == '-'){
				consecutiveDashCount++;
			}
			if(ch == '\n')
				lineNumber++;
			comment.append(ch);
			ch = (char) br.read();
		}
		
		String com = comment.toString();
		el.setqName(com.substring(0, com.length()-2));
		el.setOpenedAndClosed(true);
		el.setTagType(TAG_TYPE.COMMENT);
		return el;
	}
}
