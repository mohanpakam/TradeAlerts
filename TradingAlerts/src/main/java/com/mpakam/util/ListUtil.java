package com.mpakam.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.mpakam.model.StrategyStockQuote;

public class ListUtil
{
	// Generic function to construct a new LinkedList from ArrayList
	public static <T> List<T> getLinkedListInstance(List<T> arrayList){
		List<T> treeList = new LinkedList<>();
		if(arrayList != null)			
			for (T e: arrayList) {
				treeList.add(e);
			}
		return treeList;
	}
	// Program to convert ArrayList to LinkedList in Java
	public static void main(String args[]){
		List<String> arrayList = Arrays.asList("RED", "BLUE", "GREEN");

		// construct a new LinkedList from ArrayList
		List<String> treeList = getLinkedListInstance(arrayList);
		System.out.println(treeList);
	}
	

}