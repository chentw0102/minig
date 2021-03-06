/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.minig.server.service.impl.helper.mime;

import org.apache.james.mime4j.field.contentdisposition.parser.ContentDispositionParser;

import javax.mail.internet.MimeUtility;
import java.io.*;
import java.util.*;

/**
 * Encoder for <a href="http://tools.ietf.org/html/rfc2231">RFC2231</a> encoded parameters
 *
 * RFC2231 string are encoded as
 *
 *    charset'language'encoded-text
 *
 * and
 *
 *    encoded-text = *(char / hexchar)
 *
 * where
 *
 *    char is any ASCII character in the range 33-126, EXCEPT
 *    the characters "%" and " ".
 *
 *    hexchar is an ASCII "%" followed by two upper case
 *    hexadecimal digits.
 *
 *    TODO remove me after https://issues.apache.org/jira/browse/MIME4J-109 has been implemented
 */
class RFC2231Decoder {

	private static final byte[] decodingTable = new byte[128];
	private static final byte[] encodingTable =
			{
					(byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', (byte)'6', (byte)'7',
					(byte)'8', (byte)'9', (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F'
			};

	static {
		for (int i = 0; i < encodingTable.length; i++) {
			decodingTable[encodingTable[i]] = (byte)i;
		}
	}

	private Set<String> multisegmentNames = new HashSet<>();
	private String dispositionType = "";
	private Map<String, String> parameters = new HashMap<String, String>();

	/**
	 * A map containing the segments for all not-yet-processed multi-segment
	 * parameters. The map is indexed by "name*seg". The value object is either
	 * a String or a Value object. The Value object is not decoded during the
	 * initial parse because the segments may appear in any order and until the
	 * first segment appears we don't know what charset to use to decode any
	 * encoded segments. The segments are decoded in order in the
	 * combineMultisegmentNames method.
	 */
	private Map<String, Object> segmentList = new HashMap<>();

	public String parse(String contentDisposition) {
		String body = contentDisposition;
		ContentDispositionParser parser = new ContentDispositionParser(new StringReader(body));
		try {
			parser.parseAll();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		final String dispositionType = parser.getDispositionType();

		if (dispositionType != null) {
			this.dispositionType = dispositionType.toLowerCase(Locale.US);
			List<String> paramNames = parser.getParamNames();
			List<String> paramValues = parser.getParamValues();

			if (paramNames != null && paramValues != null) {
				final int len = Math.min(paramNames.size(), paramValues.size());
				for (int i = 0; i < len; i++) {
					String paramName = paramNames.get(i).toLowerCase(Locale.US);
					String paramValue = paramValues.get(i);
					putParameter(paramName, paramValue);
				}
				combineMultisegmentParameters();
			}
		}

		return parameters.get("filename");
	}

	/**
	 * Decode RFC2231 parameter value with charset
	 */
	private static RFC2231Value decodeRFC2231Value(String value) {
		String charset;

		RFC2231Value v = new RFC2231Value();
		v.encodedValue = value;
		v.value = value; // in case we fail to decode it

		try {
			int charsetDelimiter = value.indexOf('\'');
			if (charsetDelimiter <= 0) {
				return v; // not encoded correctly? return as is.
			}

			charset = value.substring(0, charsetDelimiter);
			int langDelimiter = value.indexOf('\'', charsetDelimiter + 1);
			if (langDelimiter < 0) {
				return v; // not encoded correctly? return as is.
			}

			value = value.substring(langDelimiter + 1);
			v.charset = charset;
			v.value = decodeRFC2231Bytes(value, charset);
		} catch (Exception e) {
			// should not happen because of isDecodingSupported check above
			throw new RuntimeException(e.getMessage(), e);
		}
		return v;
	}

	/**
	 * Decode RFC2231 parameter value without charset
	 */
	private static String decodeRFC2231Value(String value, String charset) {
		try {
			value = decodeRFC2231Bytes(value, charset);
		} catch (Exception e) {
			// should not happen because of isDecodingSupported check above
			throw new RuntimeException(e.getMessage(), e);
		}
		return value;
	}

	/**
	 * Decode the encoded bytes in RFC2231 value using the specified charset.
	 */
	private static String decodeRFC2231Bytes(String value, final String charset) throws UnsupportedEncodingException {
		/*
		 * Decode the ASCII characters in value into an array of bytes, and then
		 * convert the bytes to a String using the specified charset. We'll
		 * never need more bytes than encoded characters, so use that to size
		 * the array.
		 */
		byte[] b = new byte[value.length()];
		int i, bi;
		for (i = 0, bi = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '%') {
				String hex = value.substring(i + 1, i + 3);
				c = (char) Integer.parseInt(hex, 16);
				i += 2;
			}
			b[bi++] = (byte) c;
		}
		return new String(b, 0, bi, MimeUtility.javaCharset(charset));
	}

	/**
	 * If the name is an encoded or multi-segment name (or both) handle it
	 * appropriately, storing the appropriate String or Value object.
	 * Multi-segment names are stored in the main parameter list as an emtpy
	 * string as a placeholder, replaced later in combineMultisegmentNames with
	 * a MultiValue object. This causes all pieces of the multi-segment
	 * parameter to appear in the position of the first seen segment of the
	 * parameter.
	 */
	private void putParameter(String name, String value) {
		int star = name.indexOf('*');
		if (star < 0) {
			// single parameter, unencoded value
			parameters.put(name, value);
		} else if (star == name.length() - 1) {
			// single parameter, encoded value
			name = name.substring(0, star);
			RFC2231Value v = decodeRFC2231Value(value);
			parameters.put(name, v.value);
		} else {
			// multiple segments
			String paramName = name.substring(0, star);
			multisegmentNames.add(paramName);
			parameters.put(paramName, "");

			if (name.endsWith("*")) {
				// encoded value
				RFC2231Value valObject = new RFC2231Value();
				valObject.encodedValue = value;
				valObject.value = value; // default; decoded later

				String segmentName = name.substring(0, name.length() - 1);
				segmentList.put(segmentName, valObject);
			} else {
				// plain value
				segmentList.put(name, value);
			}
		}
	}

	/**
	 * Iterate through the saved set of names of multi-segment parameters, for
	 * each parameter find all segments stored in the slist map, decode each
	 * segment as needed, combine the segments together into a single decoded
	 * value.
	 */
	private void combineMultisegmentParameters() {
		for(String name : multisegmentNames) {
			StringBuilder paramValue = new StringBuilder();
			String charset = null;
			String segmentName;
			String segmentValue;

			// find and decode each segment
			int segment;
			for (segment = 0;; segment++) {
				segmentName = name + "*" + segment;
				segmentValue = null;
				Object v = segmentList.get(segmentName);

				if (v == null) // out of segments
					break;

				if (v instanceof RFC2231Value) {
					String encodedValue = ((RFC2231Value) v).encodedValue;
					segmentValue = encodedValue; // in case of exception

					if (segment == 0) {
						// the first segment specifies charset for all other encoded segments
						RFC2231Value vnew = decodeRFC2231Value(encodedValue);
						charset = vnew.charset;
						segmentValue = vnew.value;
					} else {
						if (charset == null) {
							// should never happen
							multisegmentNames.remove(name);
							break;
						}
						segmentValue = decodeRFC2231Value(encodedValue, charset);
					}
				} else {
					segmentValue = (String) v;
				}

				paramValue.append(segmentValue);
				//segmentList.remove(segmentName);
			}

			if (segment == 0) {
				// didn't find any segments at all
				parameters.remove(name);
			} else {
				parameters.put(name, paramValue.toString());
			}
		}

		// clear out the set of names and segments
		multisegmentNames.clear();
		segmentList.clear();
	}

	/**
	 * A struct to hold an encoded value. A parsed encoded value is stored as
	 * both the decoded value and the original encoded value (so that toString
	 * will produce the same result). An encoded value that is set explicitly is
	 * stored as the original value and the encoded value, to ensure that get
	 * will return the same value that was set.
	 */
	private static class RFC2231Value {
		public String value;
		public String charset;
		public String encodedValue;
	}
}
