package com.geoscope.GeoLog.Utils;

import java.io.IOException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TMyXML {

	public static String GetStringFromNode(Node root) throws IOException {          
		StringBuilder result = new StringBuilder();
		result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		result.append(_GetStringFromNode(root));
		return result.toString();
	}
	
	public static String _GetStringFromNode(Node root) throws IOException {          
		StringBuilder result = new StringBuilder();
		if (root.getNodeType() == 3)
			result.append(root.getNodeValue());
		else {
			if (root.getNodeType() != 9) {
				StringBuffer attrs = new StringBuffer();
				for (int k = 0; k < root.getAttributes().getLength(); ++k) {
					attrs.append(" ")
							.append(root.getAttributes().item(k).getNodeName())
							.append("=\"")
							.append(root.getAttributes().item(k).getNodeValue())
							.append("\" ");
				}
				result.append("<").append(root.getNodeName()).append(" ")
						.append(attrs).append(">");
			} 
			NodeList nodes = root.getChildNodes();
			for (int i = 0, j = nodes.getLength(); i < j; i++) {
				Node node = nodes.item(i);
				result.append(_GetStringFromNode(node));
			}
			if (root.getNodeType() != 9) {
				result.append("</").append(root.getNodeName()).append(">");
			}
		}
		return result.toString();
	} 	
	
	public static Node SearchNode(Node ParentNode, String NodeName) {
		NodeList Childs = ParentNode.getChildNodes();
		for (int I = 0; I < Childs.getLength(); I++) {
			Node Child = Childs.item(I);
			String NN = Child.getLocalName();
			if ((NN != null) && NN.equals(NodeName))
				return Child; //. =>
		}
		return null;
	}	
}
