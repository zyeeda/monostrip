package com.zyeeda.framework.ws;

import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.googlecode.genericdao.search.Search;
import com.zyeeda.framework.entities.Dictionary;
import com.zyeeda.framework.managers.DictionaryManager;
import com.zyeeda.framework.managers.internal.DefaultDictionaryManager;
import com.zyeeda.framework.ws.base.ResourceService;

@Path("/dicts")
public class DictionaryService extends ResourceService {
	
	@GET
	@Path("/{type}")
	@Produces("application/json")
	public List<Dictionary> getDictionary(@PathParam("type") String type) {
		DictionaryManager dictMgr = new DefaultDictionaryManager(this.getPersistenceService());
		Search search = new Search();
		search.addFilterEqual("type", type);
		List<Dictionary> dicts = dictMgr.search(search);
		return dicts;
	}
	
	@POST
	@Path("/")
	@Produces("application/json")
	public Dictionary createDictionary(@FormParam("") Dictionary dict) {
		DictionaryManager dictMgr = new DefaultDictionaryManager(this.getPersistenceService());
		dictMgr.persist(dict);
		this.getPersistenceService().getCurrentSession().flush();
		return dictMgr.find(dict.getId());
	}
	
	@GET
	@Path("/get_list_except")
	@Produces("application/json")
	public List<Dictionary> getListExceptId(@QueryParam("name") String name) {
		DictionaryManager dictMgr = new DefaultDictionaryManager(this.getPersistenceService());
		Search search = new Search();
		//search.addField(Filter.equal("type", "defectLevel"), Filter.notEqual("name", name));
		search.addFilterEqual("type", "defectLevel");
		search.addFilterNotEqual("name", name);
		return dictMgr.search(search);
	}
	
}
