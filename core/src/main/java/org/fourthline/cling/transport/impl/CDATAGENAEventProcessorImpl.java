package org.fourthline.cling.transport.impl;

/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import org.fourthline.cling.model.message.gena.IncomingEventRequestMessage;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableValue;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.logging.Logger;

/**
 * Default implementation based on the <em>W3C DOM</em> XML processing API with an XML processing exception for CDATA values.
 *
 * @author Christian Bauer (with modifications by Joseph Shuttlesworth)
 */
public class CDATAGENAEventProcessorImpl extends GENAEventProcessorImpl {
    private static Logger log = Logger.getLogger(CDATAGENAEventProcessorImpl.class.getName());

    // These two methods probably belong in XMLUtil.
    public static String getTextCDATAContent(Node node) {
        StringBuilder stringBuilder = new StringBuilder();
        NodeList childList = node.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (child.getNodeType() != Node.TEXT_NODE && child.getNodeType() != Node.CDATA_SECTION_NODE) {
                break;
            }

            stringBuilder.append(child.getNodeValue());
        }
        return stringBuilder.toString();
    }

    public static Boolean containsCDATA(Node node) {
        NodeList childList = node.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void readProperties(Element propertysetElement, IncomingEventRequestMessage message) {
        NodeList propertysetElementChildren = propertysetElement.getChildNodes();

        StateVariable[] stateVariables = message.getService().getStateVariables();

        for (int i = 0; i < propertysetElementChildren.getLength(); i++) {
            Node propertysetChild = propertysetElementChildren.item(i);

            if (propertysetChild.getNodeType() != Node.ELEMENT_NODE)
                continue;

            if (getUnprefixedNodeName(propertysetChild).equals("property")) {

                NodeList propertyChildren = propertysetChild.getChildNodes();

                for (int j = 0; j < propertyChildren.getLength(); j++) {
                    Node propertyChild = propertyChildren.item(j);

                    if (propertyChild.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    String stateVariableName = getUnprefixedNodeName(propertyChild);
                    for (StateVariable stateVariable : stateVariables) {
                        if (stateVariable.getName().equals(stateVariableName)) {
                            log.fine("Reading state variable value: " + stateVariableName);
                            if (containsCDATA(propertyChild)) {
                                log.fine("State variable contains CDATA.");
                            }

                            String value = getTextCDATAContent(propertyChild);
                            message.getStateVariableValues().add(
                                    new StateVariableValue(stateVariable, value)
                            );
                            break;
                        }
                    }

                }
            }
        }
    }
}
