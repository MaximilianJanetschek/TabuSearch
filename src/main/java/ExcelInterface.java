import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcelInterface {

	// workbook where to store all result
	static FileInputStream file;
	static XSSFWorkbook workbook;
	static XSSFSheet sheet;
	static int rowNumber = 3;


	public static void initializeResultSearch() {
		try {
			file = new FileInputStream(new File("Dokumentation/Results-of-Runs.xlsx"));
			workbook = new XSSFWorkbook(file);
			sheet = workbook.getSheetAt(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> readExcel() {
		ArrayList<String> instanceNames = new ArrayList<>();
		try {
			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (row.getRowNum() > 2) {
					// For each row, iterate through all the columns
					Iterator<Cell> cellIterator = row.cellIterator();

					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						if (cell.getColumnIndex() < 1) {
							// Check the cell type and format accordingly
							switch (cell.getCellType()) {
							case NUMERIC:
								System.out.print(cell.getNumericCellValue() + " | ");
								break;
							case STRING:
								instanceNames.add(cell.getStringCellValue());
								System.out.print(cell.getStringCellValue() + ", ");
								break;
							default:
								break;
							}
						}
					}
					//System.out.println("");
				}
			}
			file.close();
			System.out.println();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return instanceNames;
	}
	
	public static void writeSolutionToExcel (double [] values, int instanceNumber) {
		// Create a blank sheet
		// Blank workbook
		workbook = null;
		try {
			workbook = new XSSFWorkbook(new FileInputStream("Dokumentation/Results-of-Runs.xlsx"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Create a blank sheet
		sheet = workbook.getSheetAt(0);

		// This data needs to be written (Object[])
		Map<String, Object[]> data = new TreeMap<String, Object[]>();
		data.put("1", new Object[] {values[0], values[1],values[2], values[3],values[4], values[5],values[6], values[7],values[8], values[9]});

		// Iterate over data and write to sheet
		Set<String> keyset = data.keySet();
		int rownum = instanceNumber + 3;
		for (String key : keyset) {
			Row row = sheet.getRow(rownum++);
			Object[] objArr = data.get(key);
			int cellnum = 2;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof String)
					cell.setCellValue((String) obj);
				else if (obj instanceof Double)
					cell.setCellValue((Double) obj);
			}
		}
		try {
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File("Dokumentation/Results-of-Runs.xlsx"));
			workbook.write(out);
			out.close();
			System.out.println("data was written successfully on disk.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		rowNumber++;
	}

}