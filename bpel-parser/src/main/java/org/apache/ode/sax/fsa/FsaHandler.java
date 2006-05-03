/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.sax.fsa;

import org.apache.ode.sax.evt.Characters;
import org.apache.ode.sax.evt.EndElement;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.utils.NamespaceStack;

import javax.xml.namespace.QName;

import org.xml.sax.*;

public class FsaHandler implements ContentHandler {

  private FSA _f;
  private NamespaceStack _ns;
  private Locator _loc; 
  
  private StringBuffer _buf;
  
  public FsaHandler(FSA f) {
    _f = f;
  }
  
  /**
   * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
   */
  public void setDocumentLocator(Locator locator) {
    _loc = locator;
  }

  public void setParseContext(ParseContext pc ){
    _f.setParseContext(pc);
  }
  
  /**
   * @see org.xml.sax.ContentHandler#startDocument()
   */
  public void startDocument() throws SAXException {
    if (_f.getParseContext() == null) {
      throw new IllegalStateException(
          "The parse context must be configured prior to calling parse().");
      }
    _f.begin();
    _ns = new NamespaceStack();
    _buf = new StringBuffer();
    if (_loc != null && _loc.getSystemId() != null) {
      _f.getParseContext().setBaseUri(_loc.getSystemId());
    } else if (_loc != null && _loc.getPublicId() != null) {
      _f.getParseContext().setBaseUri(_loc.getPublicId());
    }
  }

  /**
   * @see org.xml.sax.ContentHandler#endDocument()
   */
  public void endDocument() throws SAXException {
    _f.end();
  }

  /**
   * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
   */
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    _ns.declarePrefix(prefix,uri);
  }

  /**
   * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
   */
  public void endPrefixMapping(String prefix) throws SAXException {
    // do nothing.
  }

  /**
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException {
    checkflush();
    try {
      _f.handleSaxEvent(new StartElement(
          new QName(namespaceURI,localName), new XmlAttributes(atts),
          _loc,_ns.toNSContext()));
    } catch (ParseException pe) {
      ParseError per = pe.getParseError();
      throw new SAXParseException(per.getMessage(),null,per.getLocationURI(),
          per.getLine(),per.getColumn());
    }
    _ns.pushNewContext();
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    checkflush();
    _ns.pop();
    try {
      _f.handleSaxEvent(new EndElement(
          new QName(namespaceURI,localName),_loc,_ns.toNSContext()));
    } catch (ParseException pe) {
      ParseError per = pe.getParseError();
      throw new SAXParseException(per.getMessage(),null,per.getLocationURI(),
          per.getLine(),per.getColumn());
    }
  }

  /**
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char[] ch, int start, int length) throws SAXException {
    _buf.append(ch,start,length);
  }

  /**
   * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
   */
  public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException {
    _buf.append(ch,start,length);
  }

  /**
   * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
   */
  public void processingInstruction(String target, String data)
      throws SAXException {
    checkflush();
    // TODO: What to do?
  }

  /**
   * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
   */
  public void skippedEntity(String name) throws SAXException {
    checkflush();
    // TODO: what to do?
  }
  
  private void checkflush() throws SAXException {
    if (_buf.length() != 0) {
      try {
        _f.handleSaxEvent(new Characters(_buf.toString(),_loc,_ns.toNSContext()));
      } catch (ParseException pe) {
        ParseError per = pe.getParseError();
        throw new SAXParseException(per.getMessage(),null,per.getLocationURI(),
            per.getLine(),per.getColumn());
      }
      _buf.setLength(0);
    }
  }
}
