package com.supermarket.ovenupdate.poc.infraestructure.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.supermarket.ovenupdate.poc.config.JacocoExcludedConfig;

@JacocoExcludedConfig
public class ServerTraceCache {

	private static HashMap<String, List<String>> ovensTraceCache =new HashMap<>();
	
	public static void initCache(String oven, String firstMessage) {
		if (ovensTraceCache.size() ==0) {
			List<String> newMessageList =new  ArrayList<>();
			newMessageList.add(firstMessage);
			ovensTraceCache.put(oven,newMessageList);
		}
	}
	public static void addOvenTrace(String oven, String trace) {
		if (ovensTraceCache.containsKey(oven)) {
			(ovensTraceCache.get(oven)).add(trace);
		}
	}
	
	public void deleteCache(String oven) {
		if (ovensTraceCache.containsKey(oven)) {
			ovensTraceCache.remove(oven);
		}
	}
	
	public String getNextMessageFromOven(String oven) {
		if (ovensTraceCache.containsKey(oven)) {
			List<String> messageList =ovensTraceCache.get(oven);
			return messageList.get(messageList.size()-1);
		}else {
			return null;
		}
	}
}
