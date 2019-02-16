package com.firm.orders.base.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

public class PoiHelper {

	/**
	 * 基于模板的导出方法
	 * 
	 * @param templateName
	 *            模板
	 * @param exportFileName
	 *            导出文件名称
	 * @param exportData
	 *            要导出的数据
	 * @throws Exception
	 */
	public static ResponseEntity<byte[]> exportExcel(String templateName, String exportFileName,
			List<ExportDataObject> exportData) throws Exception {
		if (exportData == null || exportData.isEmpty()) {
			throw new Exception("没有可以导出的数据!");
		}
		// 根据模板生成要导出的新文件
		File exportFile = copyFile(templateName, exportFileName);
		Workbook wb = createWorkbook(exportFile);
		// 写数据
		FileOutputStream fos = new FileOutputStream(exportFile);
		try {

			for (int i = 0; i < exportData.size(); i++) {
				if (exportData.get(i) == null) {
					continue;
				}
				// 根据模板获取excel列与业务实体属性对应关系，即第一行excel列名，第二行#+实体属性名
				Object[] attributeNames = getAttributeNamesByExcelTemplate(templateName,
						exportData.get(i).getSheetName(), 1);

				String sheetName = exportData.get(i).getSheetName();
				int sheetNumber = exportData.get(i).getSheetNumber();
				int startRow = exportData.get(i).getStartRow();
				if (null != sheetName && !sheetName.equals("")) {
					wb.setSheetName(sheetNumber - 1, sheetName);
				}

				Sheet sheet = wb.getSheetAt(sheetNumber - 1);
				if (sheet != null) {
					int dataStartRowNum = 0;
					if (startRow > 1) {
						dataStartRowNum = startRow - 1;
					}
					List<Map<String, Object>> list = exportData.get(i).getExportData();
					if (list == null || list.isEmpty()) {
						continue;
					}
					for (int m = 0; m < list.size(); m++) {
						if (list.get(m) != null) {
							Row row = null;
							if (sheet.getRow(m + dataStartRowNum) != null) {
								row = sheet.getRow(m + dataStartRowNum);
							} else {
								row = sheet.createRow(m + dataStartRowNum);
							}

							String[] columnName = null;
							if (m < attributeNames.length - 1) {
								columnName = (String[]) attributeNames[m];
							} else {
								columnName = (String[]) attributeNames[attributeNames.length - 1];
							}

							if (columnName != null && columnName.length > 0) {
								for (int j = 0; j < columnName.length; j++) {
									if (columnName[j] == null || columnName[j].equals("")) {
										continue;
									}
									for (Map.Entry<String, Object> entry : list.get(m).entrySet()) {
										Cell cell = null;
										if (row.getCell(j) != null) {
											cell = row.getCell(j);
										} else {
											cell = row.createCell(j);
										}

										if (columnName[j].contains(entry.getKey())) {
											if (columnName[j].equals(entry.getKey())) {
												setCellValue(sheet, cell, entry.getValue());
											}
										} else {
											String cellValue = getStringValueFromCell(cell);
											if (cellValue != null && !cellValue.equals("")) {
												if (cellValue.startsWith("#") || cellValue.contains("${}")) {
													setCellValue(sheet, cell, null);
												}

											}

										}
										
										 
									}
								}
							}

						}
					}

				}
				List<Map<String, Object>> mergedCellList = exportData.get(i).getExportmergedCells();
				if (mergedCellList != null && !mergedCellList.isEmpty()) {
					for (Map<String, Object> map : mergedCellList) {
						int firstRow = (int) map.get("firstRow") - 1;
						int lastRow = (int) map.get("lastRow") - 1;
						int firstCol = (int) map.get("firstCol") - 1;
						int lastCol = (int) map.get("lastCol") - 1;
						if (lastRow > firstRow) {
							setMergedRegion(sheet, firstRow, lastRow, firstCol, lastCol);
						}
						// 设置垂直居中样式
						CellStyle cellStyle = wb.createCellStyle();
						cellStyle.setBorderBottom(BorderStyle.THIN);
						cellStyle.setBorderLeft(BorderStyle.THIN);
						cellStyle.setBorderRight(BorderStyle.THIN);
						cellStyle.setBorderTop(BorderStyle.THIN);
						int lastRowNum = getSheetLastRowNum(sheet);
						short cellNum = sheet.getRow(lastRowNum).getLastCellNum();
						if(cellNum>0){
							for(int i1=0;i1<cellNum;i1++){
								Cell cell = sheet.getRow(lastRowNum).getCell(i1);
								if(cell != null){
									cell.setCellStyle(cellStyle);
								}
							}
						}
						CellStyle cellStyle1 = wb.createCellStyle();
						cellStyle1.setBorderBottom(BorderStyle.THIN);
						cellStyle1.setBorderLeft(BorderStyle.THIN);
						cellStyle1.setBorderRight(BorderStyle.THIN);
						cellStyle1.setBorderTop(BorderStyle.THIN);
						cellStyle1.setAlignment(HorizontalAlignment.CENTER);
						cellStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
						sheet.getRow(firstRow).getCell(firstCol).setCellStyle(cellStyle1);
						
						
					}
				}

			}

			wb.write(fos);
			fos.flush();
			fos.close();

			int index = exportFile.getPath().lastIndexOf("\\");
			return FileHelper.download(exportFile.getPath().substring(0, index), exportFileName, true);
		} catch (Exception e) {
			throw new Exception(e.getCause());
		} finally {
			try {
				if (null != fos) {
					fos.close();
				}
			} catch (Exception e) {
				throw new Exception(e.getCause());
			}

		}

	}

	/**
	 * 基于模板的导入方法
	 * 
	 * @param templateName
	 *            模板
	 * @param file
	 *            导入源文件
	 * @param sheetName
	 *            导入sheet名
	 * @param startRow
	 *            数据开始行
	 * @throws Exception
	 */
	public static List<Map<String, Object>> importExcel(String templateName, MultipartFile file, String sheetName,
			int startRow) throws Exception {
		if (null == file) {
			return null;
		}
		if (templateName == null || templateName.equals("")) {
			throw new Exception("模板名称不能为空!");
		}
		// 根据模板获取excel列与业务实体属性对应关系，即第一行excel列名，第二行#+实体属性名
		Object[] attributeNames = getAttributeNamesByExcelTemplate(templateName, sheetName, 1);
		// 处理文件
		List<Map<String, Object>> importDatas = new ArrayList<>();

		Workbook wb = createWorkbook(file);
		Sheet sheet =null;
		if(sheetName != null && !sheetName.equals("")){
			sheet =wb.getSheet(sheetName);
		}else{
			sheet = wb.getSheetAt(0);
		}
		if (null != sheet) {
			// 得到Excel的行数
			int totalRows = sheet.getPhysicalNumberOfRows();
			int totalCells = 0;
			// 得到Excel的列数(前提是有行数)
			if (totalRows >= 1 && sheet.getRow(0) != null) {
				totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
			}
			int dataStartRowNum = 1;
			if (startRow > 1) {
				dataStartRowNum = startRow - 1;
			}
			if (totalCells > 0) {
				for (int i = dataStartRowNum; i < totalRows; i++) {
					Map<String, Object> importData = new HashMap<>();
					Row row = sheet.getRow(i);
					if (row == null) {
						continue;
					}
					String[] columnName = (String[]) attributeNames[0];
					if (columnName != null && columnName.length > 0) {
						for (int j = 0; j < totalCells; j++) {
							if (columnName[j] == null || columnName[j].equals("")) {
								continue;
							}
							Cell cell = row.getCell(j);
							if (getStringValueFromCell(cell) == null || getStringValueFromCell(cell).equals("")) {
								continue;
							}
							importData.put(columnName[j], getStringValueFromCell(cell));
						}
					}
					importDatas.add(importData);
				}
			}

		}
		return importDatas;
	}

	public static ResponseEntity<byte[]> exportTemplateExcel(String templateName) throws Exception {
		if (templateName == null || templateName.equals("")) {
			throw new Exception("模板名称不能为空!");
		}
		// 根据模板生成要导出的新文件
		File exportFile = copyFile(templateName, templateName);
		// 根据版本选择创建Workbook的方式
		Workbook wb = createWorkbook(exportFile);
		int sheetNumber = wb.getNumberOfSheets();
		if (sheetNumber > 0) {
			for (int i = 0; i < sheetNumber; i++) {
				Sheet sheet = wb.getSheetAt(i);
				if (sheet != null) {
					String[] explicitListValues = null;
					if (sheet.getDataValidations() != null && !sheet.getDataValidations().isEmpty()) {
						explicitListValues = sheet.getDataValidations().get(0).getValidationConstraint()
								.getExplicitListValues();
					}
					int rowNum = sheet.getLastRowNum();
					if (rowNum >= 1) {
						Row row = sheet.getRow(1);
						sheet.removeRow(row);
						if (ArrayUtils.isNotEmpty(explicitListValues)) {
							setValidationData(sheet, 1, rowNum, 2, 2, explicitListValues);
						}

					}
				}
			}
		}

		// 写数据
		FileOutputStream fos = new FileOutputStream(exportFile);
		wb.write(fos);
		fos.flush();
		fos.close();

		int index = exportFile.getPath().lastIndexOf("\\");
		return FileHelper.download(exportFile.getPath().substring(0, index), templateName, true);
	}
	
	/**
	 * 获取有效行(非空列)
	 * @param sheet
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static int getSheetLastRowNum(Sheet sheet) {

		CellReference cellReference = new CellReference("A4");
		boolean flag = false;
		for (int i = cellReference.getRow(); i <= sheet.getLastRowNum();) {
			Row r = sheet.getRow(i);
			if (r == null) {
				// 如果是空行（即没有任何数据、格式），直接把它以下的数据往上移动
				sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
				continue;
			}
			flag = false;
			for (Cell c : r) {
				if (c.getCellType() != Cell.CELL_TYPE_BLANK) {
					flag = true;
					break;
				}
			}
			if (flag) {
				i++;
				continue;
			} else {// 如果是空白行（即可能没有数据，但是有一定格式）
				if (i == sheet.getLastRowNum()) {
					// 如果到了最后一行，直接将那一行remove掉
					sheet.removeRow(r);
				} else {
					// 如果还没到最后一行，则数据往上移一行
					sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
				}
			}
		}
		return sheet.getLastRowNum();

	}

	/**
	 * 赋值到excel
	 * 
	 * @param sheet
	 * @param cell
	 * @param cellVal
	 */
	public static void setCellValue(Sheet sheet, Cell cell, Object cellVal) {
		if(cell==null){
			return;
		}
		if (cellVal == null) {
			cell.setCellValue("");
		} else if (String.class.equals(cellVal.getClass())) {
			cell.setCellValue(cellVal.toString());
		} else if (Integer.class.equals(cellVal.getClass()) || int.class.equals(cellVal.getClass())) {
			cell.setCellValue(Integer.valueOf(cellVal.toString()));
		} else if (Long.class.equals(cellVal.getClass()) || long.class.equals(cellVal.getClass())) {
			cell.setCellValue(Integer.valueOf(cellVal.toString()));
		} else if (Double.class.equals(cellVal.getClass()) || double.class.equals(cellVal.getClass())) {
			cell.setCellValue(Double.valueOf(cellVal.toString()));
		} else if (Float.class.equals(cellVal.getClass()) || float.class.equals(cellVal.getClass())) {
			cell.setCellValue(Float.valueOf(cellVal.toString()));
		} else if (BigDecimal.class.equals(cellVal.getClass())) {
			cell.setCellValue(new BigDecimal(cellVal.toString()).doubleValue());
		} else if (Date.class.equals(cellVal.getClass())) {
			cell.setCellValue(new SimpleDateFormat("yyyy-MM-dd").format((Date) cellVal));
		} else if (Timestamp.class.equals(cellVal.getClass())) {
			cell.setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Timestamp) cellVal));
		} else {
			cell.setCellValue("");
		}
		cell.setCellStyle(sheet.getWorkbook().getCellStyleAt(3));
	}

	/**
	 * 将excel单元格值转为String
	 * 
	 * @param cell
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getStringValueFromCell(Cell cell) {
		SimpleDateFormat sFormat = new SimpleDateFormat("MM/dd/yyyy");
		DecimalFormat decimalFormat = new DecimalFormat("#.#");
		String cellValue = "";
		if (cell == null) {
			return cellValue;
		} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			cellValue = cell.getStringCellValue();
		}else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			if (DateUtil.isCellDateFormatted(cell)) {
				double d = cell.getNumericCellValue();
				Date date = DateUtil.getJavaDate(d);
				cellValue = sFormat.format(date);
			} else {
				cellValue = decimalFormat.format((cell.getNumericCellValue()));
			}
		} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			cellValue = "";
		} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			cellValue = String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
			cellValue = "";
		} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
			cellValue = cell.getCellFormula().toString();
		}else{
			cellValue = "";
		}
		return cellValue;
	}

	/**
	 * 根据模板中的数据得到实体属性名
	 * 
	 * @param templateName
	 * @param sheetName
	 * @param startRow
	 * @return
	 * @throws Exception
	 */
	private static Object[] getAttributeNamesByExcelTemplate(String templateName, String sheetName, int startRow)
			throws Exception {
		if (null == templateName || "".equals(templateName)) {
			throw new Exception("模板文件名不为空");
		}
		File file = getExcelTemplateFile(templateName);
		// 根据版本选择创建Workbook的方式
		Workbook wb = createWorkbook(file);
		Sheet sheet =null;
		if(sheetName==null || sheetName.equals("")){
			sheet = wb.getSheetAt(0);
		}else{
			sheet = wb.getSheet(sheetName);
		}

		if (null != sheet) {
			// 得到Excel的行数
			int totalRows = sheet.getLastRowNum()+1;
			int totalCells = 0;

			List<Object> attributeNames = new ArrayList<>();
			if (startRow > 0) {
				startRow = startRow - 1;
			}
			if (totalRows > 0) {
				for (int m = startRow; m < totalRows; m++) {
					Row row = null;
					row = sheet.getRow(m);
					if (row != null) {
						totalCells = row.getLastCellNum()+1;
						String[] columnName = new String[totalCells];

						// 循环Excel的列
						for (int i = 0; i < totalCells; i++) {
							Cell cell = row.getCell(i);
							if (getStringValueFromCell(cell).startsWith("#")) {
								columnName[i] = cell.getStringCellValue().split("#")[1];
							} else if (getStringValueFromCell(cell).startsWith("$")) {
								String express = "\\{([^}]*)\\}";
								Matcher match = Pattern.compile(express).matcher(getStringValueFromCell(cell));
								while (match.find()) {
									columnName[i] = match.group(1);
								}
							}

						}

						for (String c : columnName) {
							if (c != null && !c.equals("")) {
								attributeNames.add(columnName);
								break;
							}
						}

					}

				}
				return attributeNames.toArray(new Object[0]);
			}

		}

		return null;

	}

	private static Workbook createWorkbook(File file) throws Exception {
		Workbook wb = null;
		if (isExcel2003(file.getPath())) {
			FileInputStream in = new FileInputStream(file);
			// 使用POIFSFileSystem构造HSSFWorkbook
			POIFSFileSystem fs = new POIFSFileSystem(in);
			wb = new HSSFWorkbook(fs);
		} else if (isExcel2007(file.getPath())) {
			wb = new XSSFWorkbook(new FileInputStream(file));
		}
		return wb;
	}

	private static Workbook createWorkbook(MultipartFile file) throws Exception {
		Workbook wb = null;
		String fileName = file.getOriginalFilename();
		if (isExcel2003(fileName)) {
			// 使用POIFSFileSystem构造HSSFWorkbook
			POIFSFileSystem fs = new POIFSFileSystem(file.getInputStream());
			wb = new HSSFWorkbook(fs);
		} else if (isExcel2007(fileName)) {
			wb = new XSSFWorkbook(file.getInputStream());
		}
		return wb;
	}

	/**
	 * 是否是2003版本excel
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean isExcel2003(String filePath) {
		return filePath.matches("^.+\\.(?i)(xls)$");
	}

	/**
	 * 是否是2007版本excel
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean isExcel2007(String filePath) {
		return filePath.matches("^.+\\.(?i)(xlsx)$");
	}

	/**
	 * 根据文件名获取模板文件
	 * 
	 * @param fileName
	 *            文件名
	 * @return
	 * @throws Exception
	 */
	public static File getExcelTemplateFile(String fileName) throws Exception {
		File file = null;
		WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
		ServletContext servletContext = webApplicationContext.getServletContext();
		// 得到文件绝对路径
		String realPath = servletContext.getRealPath("/excel-template");
		file = new File(realPath + "/" + fileName);
		if (!file.exists()) {
			throw new Exception("模板文件不存在！");
		}
		return file;
	}

	/**
	 * 将指定路径下的文件复制到指定文件夹中
	 */
	public static File copyFile(String templateName, String exportFileName) throws Exception {
		File exportFile = null;
		File file = getExcelTemplateFile(templateName);
		String path = file.getPath().substring(0, file.getPath().lastIndexOf("\\") + 1);
		// 指定文件夹路径
		String exportFileFolderPath = path + "temp\\";
		// 指定文件夹
		File exportFileFolder = new File(exportFileFolderPath);

		try {
			// 判断文件夹是否存在,不存在需要创建，否则无法正常创建该文件夹下的文件
			if (!exportFileFolder.exists()) {
				exportFileFolder.mkdirs();
			}
			// 复制后文件的路径与命名
			if (exportFileName.indexOf(".") == -1) {
				exportFileName = exportFileName + "." + file.getName().split(".")[1];
			}
			String exportFilePath = exportFileFolderPath + exportFileName;
			exportFile = new File(exportFilePath);
			if (!exportFile.exists()) {
				exportFile.createNewFile();
			}
			// 复制内容到指定文件中
			copyFile(file, exportFile);
		} catch (Exception e) {
			throw new Exception("复制文件方法，" + e.getMessage());
		}

		return exportFile;
	}

	/**
	 * 复制模板
	 * 
	 * @param template
	 * @param newFile
	 * @throws Exception
	 */
	public static void copyFile(File template, File newFile) throws Exception {
		InputStream in = null;
		OutputStream out = null;
		try {

			in = new BufferedInputStream(new FileInputStream(template));
			out = new BufferedOutputStream(new FileOutputStream(newFile));
			if (in != null && in.available() > 0) {
				byte[] buffer = new byte[in.available()];
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
			}
			in.close();
			out.close();

		} catch (Exception e) {
			throw new Exception("复制模板方法," + e.getMessage());
		} finally {
			if (null != in) {
				in.close();
			}
			if (null != out) {
				out.close();
			}
		}

	}

	/**
	 * int转byte数组
	 * 
	 * @param bytes
	 * @return
	 */
	public static byte[] IntToByte(int num) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((num >> 24) & 0xff);
		bytes[1] = (byte) ((num >> 16) & 0xff);
		bytes[2] = (byte) ((num >> 8) & 0xff);
		bytes[3] = (byte) (num & 0xff);
		return bytes;

	}

	public class ExportDataObject implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private String sheetName;
		private int sheetNumber;
		private int startRow;
		private List<Map<String, Object>> exportmergedCells;
		private List<Map<String, Object>> exportData;

		public String getSheetName() {
			return sheetName;
		}

		public void setSheetName(String sheetName) {
			this.sheetName = sheetName;
		}

		public int getSheetNumber() {
			return sheetNumber;
		}

		public void setSheetNumber(int sheetNumber) {
			this.sheetNumber = sheetNumber;
		}

		public List<Map<String, Object>> getExportData() {
			return exportData;
		}

		public void setExportData(List<Map<String, Object>> exportData) {
			this.exportData = exportData;
		}

		public int getStartRow() {
			return startRow;
		}

		public void setStartRow(int startRow) {
			this.startRow = startRow;
		}

		public List<Map<String, Object>> getExportmergedCells() {
			return exportmergedCells;
		}

		public void setExportmergedCells(List<Map<String, Object>> exportmergedCells) {
			this.exportmergedCells = exportmergedCells;
		}
		
		

	}

	/**
	 * 添加数据有效性检查.
	 * 
	 * @param sheet
	 *            要添加此检查的Sheet
	 * @param firstRow
	 *            开始行
	 * @param lastRow
	 *            结束行
	 * @param firstCol
	 *            开始列
	 * @param lastCol
	 *            结束列
	 * @param explicitListValues
	 *            有效性检查的下拉列表
	 * @throws IllegalArgumentException
	 *             如果传入的行或者列小于0(< 0)或者结束行/列比开始行/列小
	 */
	public static void setValidationData(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol,
			String[] explicitListValues) throws IllegalArgumentException {
		if (firstRow < 0 || lastRow < 0 || firstCol < 0 || lastCol < 0 || lastRow < firstRow || lastCol < firstCol) {
			throw new IllegalArgumentException(
					"Wrong Row or Column index : " + firstRow + ":" + lastRow + ":" + firstCol + ":" + lastCol);
		}
		if (sheet instanceof XSSFSheet) {
			XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
			XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
					.createExplicitListConstraint(explicitListValues);
			CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
			XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
			validation.setSuppressDropDownArrow(true);
			validation.setShowErrorBox(true);
			sheet.addValidationData(validation);
		} else if (sheet instanceof HSSFSheet) {
			CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
			DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint(explicitListValues);
			DataValidation validation = new HSSFDataValidation(addressList, dvConstraint);
			validation.setSuppressDropDownArrow(true);
			validation.setShowErrorBox(true);
			sheet.addValidationData(validation);
		}
	}
	/**
	 * 设置合并单元格(下标从0开始)
	 * @param sheet sheet
	 * @param firstRow 起始行号
	 * @param lastRow 终止行号
	 * @param firstCol 起始列号
	 * @param lastCol 终止列号
	 * @throws Exception
	 */
	 public static void setMergedRegion(Sheet sheet,int firstRow,int lastRow,int firstCol,int lastCol) throws Exception{
		 CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
		 sheet.addMergedRegion(region);
	 }
	
	public static void main(String[] args) {

	}
}
