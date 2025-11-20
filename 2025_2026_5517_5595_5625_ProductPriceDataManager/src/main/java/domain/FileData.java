package domain;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;

import dto.MeasurementDTO;
import dto.ProductDTO;
import dto.YearDTO;

public class FileData{
	
	
	private ArrayList <String> productNames;
	private ArrayList<ProductDTO> productsArrayList;
	private HashMap<String, ProductDTO> dictProducts;
	
	private ArrayList<YearDTO> yearsArrayList;
	private HashMap<Integer, YearDTO> dictYears;
	
	
	public FileData() {
		productNames = new ArrayList<>();
		productsArrayList = new ArrayList<>();
		dictYears = new HashMap<>();
		yearsArrayList = new ArrayList<>();
		dictProducts = new HashMap<>();
	}
	
	public void addProductName(String name) {
		productNames.add(name);
	}
	
	public void addYearToDict(YearDTO year) {
		dictYears.put(year.getYear(), year);
	}

	// Private Inner Engine
	private void prodFactory() {
		ArrayList<MeasurementDTO> p_measurements;
		ProductDTO product;
	
		int p_index = 0;
		for(String productName: productNames) {
			
			p_measurements = new ArrayList<>();
			for(YearDTO year: yearsArrayList) {
				p_measurements.add(year.getMeasurements().get(p_index));
			}
			
			productsArrayList.add(new ProductDTO(productName, p_measurements));
			p_index++;
		}
		
		for(ProductDTO prod: productsArrayList) {
			dictProducts.put(prod.getName(), prod);
		}
	}
	
	
	
	
	
	
	
	// Getters 
	public String getProductName(int index) {
		return productNames.get(index);
	}
	
	public ProductDTO getProductDTO(String product) {
		return dictProducts.get(product);
	}
	
	
	public YearDTO getYearDTO(int year) {
		return dictYears.get(year);
	}
	
	public ArrayList<YearDTO> getAllYears(){
		if(yearsArrayList.size() == 0){
			for(YearDTO year: dictYears.values()) {
				yearsArrayList.add(null);
			}
		}
		return yearsArrayList;
	}
	
	public ArrayList<ProductDTO> getAllProducts(){
		if(productsArrayList.size()==0) {
			this.prodFactory();
		}
		return productsArrayList;
	}
}