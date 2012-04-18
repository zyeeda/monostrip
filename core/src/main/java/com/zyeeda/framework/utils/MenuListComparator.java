package com.zyeeda.framework.utils;

import java.util.Comparator;
import com.zyeeda.framework.viewmodels.MenuVo;

public class MenuListComparator implements Comparator<Object> {
	

	@Override
	public int compare(Object o1, Object o2) {
		MenuVo menu = (MenuVo)o1;
		int flag = 0;
		MenuVo menu1 = (MenuVo)o2;
		if(menu != null && menu1 != null) {
			String menuOrder1 = menu.getOrderBy();
			String menuOrder2 = menu.getOrderBy();
			if(menuOrder1 != null && menuOrder2 != null){
				flag = menu.getOrderBy().compareTo(menu1.getOrderBy());
			}
		}
		return flag;
	}
	
}
