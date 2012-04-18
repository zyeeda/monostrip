package com.zyeeda.framework.ws;

import static com.zyeeda.framework.ws.DocumentServiceHelper.document2Vo;
import static com.zyeeda.framework.ws.DocumentServiceHelper.documentList2Vo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import com.zyeeda.framework.entities.Document;
import com.zyeeda.framework.managers.DocumentException;
import com.zyeeda.framework.managers.DocumentManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.managers.internal.MongoDbDocumentManager;
import com.zyeeda.framework.viewmodels.DocumentVo;
import com.zyeeda.framework.viewmodels.DocumentsVo;
import com.zyeeda.framework.ws.base.ResourceService;

@Path("/docs")
public class DocumentService extends ResourceService {

	private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
	
	private final static String DEFAULT_CHARSET = "GBK";

	//private static final int OBJECT_ID_LENGTH = 24;
	//private static final int DISPLAY_PAGE_NUMBER = 200;

	@POST
	@Path("/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("text/json")
	public DocumentVo upload(MultipartBody body, @QueryParam("foreignId") String foreignId) throws DocumentException, UserPersistException {
		
		logger.debug("foreign id = {}", foreignId);
		
		DocumentManager docMgr = new MongoDbDocumentManager(this.getMongoDbService());
		Document doc = new Document();
		doc.setTemp(true);
		String userName = this.getChinaName();
		String currentUser = this.getSecurityService().getCurrentUser();
		doc.setOwner(currentUser); // 拥有人
		doc.setCreator(currentUser); // 创建人
		doc.setLastModifier(currentUser);// 最后修改人
		doc.setForeignId(foreignId);
		doc.setOwnerChinaName(userName);
		Date now = new Date();
		doc.setCreatedTime(now);// 创建时间
		doc.setLastModifiedTime(now);// 最后修改时间
		InputStream is = null;
		List<String> list = new ArrayList<String>();
		List<Attachment> attaches = body.getAllAttachments();
		try {
			for (Attachment attach : attaches) {
				String key = attach.getContentDisposition().getParameter("name");
				logger.debug("this key of attachment is : {}", key);
				
				Map<String, ?> keys = attach.getContentDisposition().getParameters();
				Set<String> allKeys = keys.keySet();
				for(String key1 : allKeys){
					System.out.println("====keys["+key1+"]=" + keys.get(key1));
				}
				
				if ("Filedata".equals(key)) {
					MediaType contentType = attach.getContentType();
					logger.debug("content type = {} ", contentType.toString());
					doc.setContentType(contentType.toString());
					doc.setSubType(contentType.getSubtype());
					doc.setPrimaryType(contentType.getType());
					is = attach.getDataHandler().getInputStream();
					logger.debug("filesize = {}", is.available());
					doc.setContent(is);
				} else if ("Filename".equals(key)) {
					String value = IOUtils.toString(attach.getDataHandler().getInputStream(), "UTF-8");
					list.add(value);
					doc.setName(value);
					String fileType = StringUtils.substringAfterLast(value, ".");
					doc.setFileType(fileType);
					if (logger.isDebugEnabled()) {
						logger.debug("multipart: fileName = {}", value);
						logger.debug("file type = {}", value);
					}
				} else if("Remarks".equals(key)) {
					String value = IOUtils.toString(attach.getDataHandler().getInputStream(), "UTF-8");
					logger.debug("value is={}",value);
					BasicDBObject obj = (BasicDBObject) JSON.parse(value);
					//BasicDBObject objStr = (BasicDBObject)obj.get("Remarks");
					//BasicDBObject objStr = (BasicDBObject) JSON.parse(obj.get("Remarks").toString());
					for(String desc : list) {
						logger.debug("remarks value is : {}" , obj.get(desc));
						doc.setDescription((obj.get(desc)).toString());
					}
				}
			}
			if (MediaType.APPLICATION_OCTET_STREAM.equals(doc.getContentType())) {
				String contentType = URLConnection.guessContentTypeFromName(doc.getName());
				logger.debug("guess content type from name = {}", contentType);
				doc.setContentType(contentType);
			}
			
			docMgr.persist(doc);
			
			return document2Vo(docMgr.findById(doc.getId(), false));
		} catch (IOException e) {
			throw new DocumentException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Saving document failed.", e);
				}
			}
		}
	}
		
	@GET
	@Path("/{id}/download/{fileName}")
	@Produces("multipart/mixed")
	public MultipartBody download(@PathParam("id") String id, @PathParam("fileName") String fileName) throws DocumentException {
		if (logger.isDebugEnabled()) {
			logger.debug("id = {}", id);
			logger.debug("file name = {}", fileName);
		}
		
		DocumentManager docMgr = new MongoDbDocumentManager(this.getMongoDbService());
		InputStream is = null;
		try {
			Document doc = docMgr.findByIdAndFileName(id, fileName);
			if (doc == null) {
				throw new DocumentException("找不到指定文件");
			}
			
			logger.debug("document content type = {}", doc.getContentType());
			is = doc.getContent();
			Attachment attach = new Attachment(id, MediaType.APPLICATION_OCTET_STREAM, is);
			List<Attachment> attaches = new ArrayList<Attachment>();
			attaches.add(attach);
			return new MultipartBody(attaches, true);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Error occured when closing file stream.", e);
				}
			}
		}
	}

	@GET
	@Path("/{id}/view/{fileName}")
	@Produces("multipart/mixed")
	public Attachment view(@PathParam("id") String id, @PathParam("fileName") String fileName) throws DocumentException {
		
		if (logger.isDebugEnabled()) {
			logger.debug("id = {}", id);
			logger.debug("file name = {}", fileName);
		}
		DocumentManager docMgr = new MongoDbDocumentManager(this.getMongoDbService());
		InputStream is = null;
		try {
			Document doc = docMgr.findByIdAndFileName(id, fileName);
			if (doc == null) {
				throw new DocumentException("找不到指定文件");
			}
			
			String contentType = doc.getContentType();
			logger.debug("document content type = {}", contentType);
			if (StringUtils.startsWith(contentType, "text")) {
				contentType = String.format("%s;charset=%s", doc.getContentType(), DEFAULT_CHARSET);
			}
			Attachment attach = new Attachment(id, contentType, doc.getContent());
		
			return attach;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Error occured when closing file stream.", e);
				}
			}
		}
	}
	
	@DELETE
	@Path("/{id}")
	@Produces("application/json")
	public void deleteById(@PathParam("id") String id) {
		logger.debug("id = {}", id);
		DocumentManager documentManager = new MongoDbDocumentManager(this.getMongoDbService());
		documentManager.removeById(id);
	}
	
	@POST
	@Path("/remarks")
	@Produces("application/json")
	public void addRemarks(@QueryParam("fileID") String fileID, @QueryParam("filename") String filename, @QueryParam("remarks") String remarks) {
		logger.debug("add remarks fileID = {}, remarks = {}", fileID, remarks);
		DocumentManager docMgr = new MongoDbDocumentManager(this.getMongoDbService());
		Document doc = docMgr.findByIdAndFileName(fileID, filename);
		doc.setDescription(remarks);
		try {
			docMgr.updateDocument(doc);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@GET
	@Path("/count")
	@Produces(MediaType.TEXT_PLAIN)
	public long countByForeignId(@QueryParam("foreignId") String foreignId) {
		logger.debug("foreign id = {}", foreignId);
		DocumentManager docMgr = new MongoDbDocumentManager(this.getMongoDbService());
		return docMgr.countByForeignId(foreignId);
	}
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public DocumentsVo listByForeignId(@QueryParam("foreignId") String foreignId, @Context UriInfo ui) {
		logger.debug("foreign id = {}", foreignId);
		DocumentManager docMgr = new MongoDbDocumentManager(this.getMongoDbService());
		List<Document> docs = docMgr.findByForeignId(foreignId);
		return documentList2Vo(docs);
	}
	
	
	
	/*@GET
	@Path("/list/{page}")
	@Produces(MediaType.APPLICATION_JSON)
	public DocumentsVo listByForeignId(@QueryParam("foreginId") String foreginId,
			@QueryParam("keyword") String keyword, @PathParam("page") int page) throws DocumentException {
		
		int skip = (page - 1) * DISPLAY_PAGE_NUMBER;
		int limit = page * DISPLAY_PAGE_NUMBER;
		
		DocumentManager docMgr = new MongoDbDocumentManager(this.getMongoDbService());
		List<Document> docs = docMgr.findByForeignIdAndKeyword(String foreignId, String keyword, int skip, int limit);
		
		List<DocumentVo> docVos = new ArrayList<DocumentVo>(docs.size());
		for (int i = 0; i < docs.size(); i++) {
			Document doc = docs.get(i);
			DocumentVo docVo = document2Vo(doc);
			docVos.add(docVo);
		}
		
		DocumentsVo docsVo = new DocumentsVo();
		docsVo.setCurrentPage(page);
		docsVo.setTotalCount(docMgr.countByForeignIdAndKeyword(String foreignId, String keyword));
		docsVo.setTotalPage(Math.round( docsVo.getTotalCount() / DISPLAY_PAGE_NUMBER));
		docsVo.setKeyword(keyword);
		docsVo.setDocs(docVos);
		
		return docsVo;
	}*/
	
	/*@GET
	@Path("/{owner}/{page}")
	@Produces("application/json")
	public List<DocumentsVo> findByOwnerAndForeignId(
			@QueryParam("keyword") String keyword,
			@QueryParam("foreignId") String foreignId,
			@PathParam("owner") String owner, @PathParam("page") int page)
			throws DocumentException {

		int skip = (page - 1) * DISPLAY_PAGE_NUMBER;
		int limit = page * DISPLAY_PAGE_NUMBER;

		DocumentManager docMgr = new MongoDbDocumentManager(this.getMongoDbService());
		List<Document> list = null;
		int documentNumber = 0;
		String[] key = keyword.split(",");
		logger.debug("findByOwnerAndForeignId skip={},keyword={}", skip,
				keyword);
		logger.debug("foreignId = {} ,owner = {}", foreignId, owner);
		// 有关键字的查询
		if (StringUtils.isNotBlank(keyword)) {
			logger
					.debug("findByOwnerAndForeignId run findByKeyword have keyword");
			list = docMgr.findByKeyword(owner, key, foreignId, skip,
					DISPLAY_PAGE_NUMBER);
			documentNumber = docMgr.findNumber(owner, foreignId, key);
		} else {
			logger
					.debug("findByOwnerAndForeignId run findByKeyword not keyword");
			list = docMgr.findByKeyword(owner, foreignId, skip,
					DISPLAY_PAGE_NUMBER);
			documentNumber = docMgr.findOwnerNumber(owner, foreignId);
		}
		List<DocumentsVo> documentList = new ArrayList<DocumentsVo>();

		documentList = this.getDocumentListVo(list, documentNumber, page);
		logger
				.debug("++++++++++++ documentList size = {}", documentList
						.size());
		return documentList;
	}*/

	/*@GET
	@Path("/{id}")
	@Produces("application/json")
	public Document findById(@PathParam("id") String id)
			throws DocumentException {
		DocumentManager documentManager = new MongoDbDocumentManager(this.getMongoDbService());
		Document document = documentManager.findById(id);
		return document;
	}

	@PUT
	@Path("/")
	@Produces("application/json")
	public void editDocument(@FormParam("") Document document)
			throws UnknownHostException, DocumentException {
		String currentUser = this.getSecurityService().getCurrentUser();
		document.setLastModifier(currentUser);
		document.setLastModifiedTime(new Date());
		DocumentManager documentManager = new MongoDbDocumentManager(this.getMongoDbService());
		documentManager.updateDocument(document);
	}

	

	// 如果没有ID给出提示
	@DELETE
	@Path("/allRemove")
	@Produces("application/json")
	public void allRemove(@Parameter(name = "allId") String allId)
			throws DocumentException, UnknownHostException {
		DocumentManager documentManager = new MongoDbDocumentManager();

		String[] id = allId.split(",");
		logger.debug(" allRemove id= {},idsize={}", id, id.length);

		documentManager.allremoveById(id);

	}
	
	

	private List<DocumentsVo> getDocumentListVo(List<Document> list,
			int documentNumber, int page) {
		List<DocumentsVo> documentListVo = new ArrayList<DocumentsVo>(
				list.size());
		for (int i = 0; i < list.size(); i++) {
			DocumentsVo docListVo = new DocumentsVo();
			Document document = list.get(i);
			docListVo.setId(document.getId());
			docListVo.setName(document.getName());
			docListVo.setDescription(document.getDescription());
			docListVo.setCreator(document.getCreator());
			docListVo.setCreatedTime(document.getCreatedTime());
			docListVo.setLastModifier(document.getLastModifier());
			docListVo.setLastModifiedTime(document.getLastModifiedTime());
			docListVo.setForeignId(document.getForeignId());
			docListVo.setOwner(document.getOwner());
			docListVo.setFileSize(document.getFileSize());
			docListVo.setFileType(document.getFileType());
			docListVo.setKeyword(document.getKeyword());
			docListVo.setContentType(document.getContentType());
			docListVo.setSubType(document.getSubType());
			docListVo.setType(document.getType());
			docListVo.setCurrentlyPage(page);
			docListVo.setDocumentNumber(documentNumber);

			docListVo.setWeight(document.getWeight());
			String deleteUrl = "/rest/docs/" + document.getId();
			docListVo.setDeleteUrl(deleteUrl);
			String downloadUrl = "/rest/docs/" + document.getId()
					+ "/download/" + document.getName();
			docListVo.setDownloadUrl(downloadUrl);
			String viewUrl = "/rest/docs/" + document.getId() + "/view/"
					+ document.getName() + "";
			docListVo.setViewUrl(viewUrl);
			String viewByIdUrl = "/rest/docs/" + document.getId() + "";
			docListVo.setViewByIdUrl(viewByIdUrl);

			float length = document.getFileSize();
			if (length / KB >= 1 && length / KB < 1024) {
				docListVo.setSize(String.format("%.2f", length / KB) + "KB");
			} else if (length / MB >= 1 && length / MB < 1024) {
				docListVo.setSize(String.format("%.2f", length / MB) + "MB");
			} else if (length / GB >= 1) {
				docListVo.setSize(String.format("%.2f", length / GB) + "GB");
			} else {
				docListVo.setSize(length + "byte");
			}
			documentListVo.add(docListVo);
		}
		return documentListVo;
	}*/
}
