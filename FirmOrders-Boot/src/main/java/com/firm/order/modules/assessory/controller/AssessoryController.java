package com.firm.order.modules.assessory.controller;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.firm.order.modules.assessory.entity.AssessoryEntity;
import com.firm.order.modules.assessory.service.IAssessoryService;
import com.firm.order.modules.assessory.vo.AssessoryVO;
import com.firm.order.utils.FileHelper;
import com.firm.order.utils.JsonBackData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(value="assessory")
public class AssessoryController {

	@Autowired
	private IAssessoryService service;
	
	@RequestMapping(value = "delete",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData delete(@RequestBody Map<String,Object> map) {
		JsonBackData back = new JsonBackData();
		try {
			if(!map.containsKey("id")){
				throw new Exception("id不能为空！");
			}
			service.delete((String)map.get("id"));
			back.setSuccess(true);
			back.setBackMsg("删除成功！");

		} catch (Exception e) {
			log.error("附件删除方法：", e);
			back.setSuccess(false);
			back.setBackMsg("删除失败," + e.getMessage());
		}
		return back;
	}
	 
	@RequestMapping(value="upload",method = RequestMethod.POST)
	@ResponseBody
    public JsonBackData upload(HttpServletRequest request,@RequestParam(required=false) String jumpUrl,@RequestParam(required=false) String fileName,@RequestParam(required=false) String description,@RequestParam(required=false) String businessType,@RequestParam MultipartFile bannerImage)  {
		JsonBackData back = new JsonBackData();
		
		try {	
			// 如果文件不为空，写入上传路径
			if (bannerImage.getOriginalFilename() !=null && !bannerImage.getOriginalFilename().equals("")) {
				// 上传文件路径
				String rootpath = request.getServletContext().getRealPath("");
				String filePath = rootpath + "/assessory/";
				fileName = FileHelper.upload(filePath,bannerImage);
				AssessoryVO vo = new AssessoryVO();
				String contextPath = request.getContextPath()+"/assessory/";
				vo.setPath(contextPath.replaceAll("\\\\", "/"));
				vo.setType(fileName.substring(fileName.lastIndexOf(".") + 1));
				vo.setName(fileName);
				vo.setBusinessType(businessType);
				vo.setDescription(description);
				vo.setUrl("/assessory/download?fileName="+fileName);
				vo.setJumpUrl(jumpUrl);
				AssessoryVO reVo = service.save(vo, AssessoryEntity.class,
						AssessoryVO.class);
				back.setBackData(reVo);
				back.setSuccess(true);
				back.setBackMsg("上传成功！");
			} else {
				back.setSuccess(false);
				back.setBackMsg("没有文件可以上传！");
			}
		} catch (Exception e) {
			log.error("附件上传方法：", e);
			back.setSuccess(false);
			back.setBackMsg("上传失败," + e.getMessage());
		}
		return back;
	}
	@RequestMapping(value = "download")
	public ResponseEntity<byte[]> download(HttpServletRequest request, @RequestParam("fileName") String fileName) throws Exception {
		// 下载文件路径
		String rootpath = request.getServletContext().getRealPath("");
		String filePath = rootpath + "/assessory/";
		return FileHelper.download(filePath, fileName,false);
	}
	
	@SuppressWarnings("deprecation")
	@RequestMapping(value = "queryByType")
	@ResponseBody
	public JsonBackData queryByType(@RequestParam Map<String,Object> map) {
		JsonBackData back = new JsonBackData();
		try {
			Pageable pageable = null;
			String pageNumber = (String) map.get("pageNumber");
			String pageSize = (String) map.get("pageSize");
			String sortField = (String) map.get("sortField");
			String sortType = (String) map.get("sortType");
			if (!StringUtils.isEmpty(pageNumber) && !StringUtils.isEmpty(pageSize)) {
				int iPageNumber = Integer.parseInt(pageNumber);
				if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortType)) {
					pageable = new PageRequest(iPageNumber <= 0 ? 0 : (iPageNumber - 1), Integer.parseInt(pageSize),
							Direction.fromString(sortType), sortField);
				} else {
					pageable = new PageRequest(iPageNumber <= 0 ? 0 : (iPageNumber - 1), Integer.parseInt(pageSize));
				}
			}
			Page<AssessoryVO> reVO = service.queryByType(pageable,map);
			
			back.setBackData(reVO);
			back.setSuccess(true);
			back.setBackMsg("根据type查询附件成功！");

		} catch (Exception e) {
			log.error("根据type查询附件方法：", e);
			back.setSuccess(false);
			back.setBackMsg("根据type查询附件失败," + e.getMessage());
		}
		return back;
	}

}
