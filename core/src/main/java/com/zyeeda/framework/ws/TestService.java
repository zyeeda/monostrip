package com.zyeeda.framework.ws;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.zyeeda.framework.entities.TestEntity;
import com.zyeeda.framework.ws.base.ResourceService;

@Path("/tests")
public class TestService extends ResourceService {

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TestEntity> getList() {
		List<TestEntity> list = new ArrayList<TestEntity>();
		
		TestEntity e = new TestEntity();
		e.setName("name1");
		
		TestEntity e2 = new TestEntity();
		e2.setName("name2");
		TestEntity e3 = new TestEntity();
		e3.setName("name3");
		
		List<TestEntity> list2 = new ArrayList<TestEntity>();
		list2.add(e2);
		list2.add(e3);
		e.setChildren(list2);
		
		TestEntity e4 = new TestEntity();
		e4.setName("name4");
		
		List<TestEntity> list3 = new ArrayList<TestEntity>();
		list3.add(e4);
		e3.setChildren(list3);
		
		list.add(e);
		return list;
	}

}
