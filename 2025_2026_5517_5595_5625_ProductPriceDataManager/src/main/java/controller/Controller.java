package controller;

import dto.ProductDTO;
import dto.ProductHighlightDTO;
import dto.ProductStatsDTO;
import dto.Top10AppearanceDTO;
import dto.YearDTO;
import dto.CategoryHighlightDTO;
import dto.MeasurementDTO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Controller implements IController
{

	private ArrayList<ArrayList<String>> dataArray;
	private ArrayList<ArrayList<String>> metadataArray;
	private ArrayList<String> productNames = new ArrayList<String>();
	private HashMap<String,String> dictAliases = new HashMap<String,String>(); // product Name to Alias
	private HashMap<String,String> dictCategories = new HashMap<String,String>(); // product Name to Category
	private ArrayList<YearDTO> yearsArrayList=new ArrayList<YearDTO>();
	private HashMap<Integer, YearDTO> dictYears= new HashMap<Integer, YearDTO>();
	private ArrayList<ProductDTO> productsArrayList= new ArrayList<ProductDTO>();
	private HashMap<String, ProductDTO> dictProducts= new HashMap<String, ProductDTO>();
	
	private int startingYear;
	@SuppressWarnings("unused")
	private int productNumber;
	
	@Override
	public int initializeFromIni(String iniPath, String delimiter) {
		
				// Load the config.ini file and store the properties 
		int numOfYears = 0;
				Properties props = new Properties();
				try {
				props.load(new FileInputStream(iniPath));
				}
				catch (IOException e) {
					e.printStackTrace();
				    }
				
				String dataFile = props.getProperty("dataFile");
				String metadataFile = props.getProperty("metadataFile");
				
				try {
					dataArray=readFile(dataFile,delimiter);
					numOfYears=dataArray.size();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					metadataArray=readFile(metadataFile,delimiter);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				organizeData();
				return numOfYears;
	}

	private ArrayList<ArrayList<String>>  readFile(String filepath, String delimiter) throws IOException{
		ArrayList<ArrayList<String>> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		        String[] values = line.split(delimiter);
		        
		        records.add(new ArrayList<String> (Arrays.asList(values)));
		    }
		}
		return records;
	}
	
	@Override
	public void loadFile(String path, String delimiter) throws IOException {
		try {
			dataArray=readFile(path,delimiter);
			organizeData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void organizeData()
	{
		getProductNames();
		getYearData();
		getProductData();
		
	}
	
	private void getProductNames()
	{
		productNames.clear();
		
		if (dataArray.size()>0)
		{
			for (int i = 1; i < dataArray.get(0).size() - 2; i++) {
			  productNames.add(dataArray.get(0).get(i));
			  productNumber++;
		
			}
			
			if(metadataArray.size()>0) {
				for (int i = 0; i < metadataArray.size(); i++) {
					dictAliases.put(productNames.get(i), metadataArray.get(i).get(1)); // Aliases
					dictCategories.put(productNames.get(i), metadataArray.get(i).get(2)); // Categories
				}
			}
			
		}
	}
	
	private void getYearData()
	{
		yearsArrayList.clear();
		Integer year;
		YearDTO ydto;
		  
		String product;
		Double value;
		MeasurementDTO mr;

        String[] top10_arr;
        String[] headline_arr;
        ArrayList<String> commodityTop10;
        ArrayList<String> headline;

		ArrayList<MeasurementDTO> values;
		
		for (int i = 1; i < dataArray.size(); i++) 
		{
			 year = Integer.parseInt(dataArray.get(i).get(0));
			 values = new ArrayList<MeasurementDTO>();
				for (int j = 1; j < dataArray.get(i).size()-2; j++) 
				{
					 product=dictAliases.get(dataArray.get(0).get(j));
					 value = Double.parseDouble(dataArray.get(i).get(j));
					 mr = new MeasurementDTO(year, product, value);
					 values.add(mr);
				}
				
				top10_arr = dataArray.get(i).get(dataArray.get(i).size()-2).replaceAll("\"", "").split("\\s*,\\s*");
		        headline_arr = dataArray.get(i).get(dataArray.get(i).size()-1).split("[|]");
		        commodityTop10 = new ArrayList<>(Arrays.asList(top10_arr));
	            headline = new ArrayList<>(Arrays.asList(headline_arr));
	            ydto=new YearDTO(year, values, commodityTop10, headline);
	            yearsArrayList.add(ydto);
	            dictYears.put(year,ydto);
		}
		startingYear = yearsArrayList.get(0).getYear();
	}
	
	@Override
	public List<YearDTO> listYears() {
		
		return yearsArrayList;
	}

	@Override
	public List<ProductDTO> listProducts() {
		
		return productsArrayList;
	}

	private void getProductData() {
		ArrayList<MeasurementDTO> p_measurements;
		int p_index = 0;
		for(Map.Entry<String, String> entry : dictAliases.entrySet()) {
			
			p_measurements = new ArrayList<>();
			for(YearDTO year: yearsArrayList) {
				p_measurements.add(year.getMeasurements().get(p_index));
			}
			
			productsArrayList.add(new ProductDTO(entry.getValue(), p_measurements));
			p_index++;
		}
		
		for(ProductDTO prod: productsArrayList) {
			dictProducts.put(prod.getName(), prod);
		}
	}
	
	@Override
	public YearDTO getYearMeasurements(int year) {
		
		return dictYears.get(year);
	}

	@Override
	public ProductDTO getProductMeasurements(String productName) {
		
		return dictProducts.get(dictAliases.get(productName));
	}

	@Override
	public ProductDTO filterProductMeasurements(String productName, int minYear, int maxYear) {
		
		ProductDTO product = dictProducts.get(productName);
		ArrayList<MeasurementDTO> allMeasurements = (ArrayList<MeasurementDTO>) product.getMeasurements();
		ArrayList<MeasurementDTO> filteredMeasurements = new ArrayList<>();
		
		for(int i = (minYear - startingYear) ; i < (maxYear - startingYear) + 1; i++) {
			filteredMeasurements.add(allMeasurements.get(i));
		}
		return new ProductDTO(productName, filteredMeasurements);
	}
	
	
	
	@Override
	public List<ProductHighlightDTO> reportProductHighlights(String productAlias) {
		ArrayList<String> curr_line;
		ArrayList<ProductHighlightDTO> highlightArrayList = new ArrayList<>();
		ProductHighlightDTO prodHighlight;
		
		for(YearDTO year: yearsArrayList) {
			curr_line = (ArrayList<String>) year.getTop10Headlines();
			for (int i = 0; i < curr_line.size() ; i++) {
				if (curr_line.get(i).contains(productAlias)) {
					prodHighlight = new ProductHighlightDTO(year.getYear(),curr_line.get(i));
					highlightArrayList.add(prodHighlight);
				}
			}
		}
		return highlightArrayList;
	}

	@Override
	public List<CategoryHighlightDTO> reportCategoryHighlights(String category) {
		ArrayList<String> curr_line;
		ArrayList<CategoryHighlightDTO> catHighlightArrayList = new ArrayList<>();
		ArrayList<String> productsFromCategory = new ArrayList<>();
		
		
		for(Map.Entry<String, String> entry : dictCategories.entrySet()) {
			if(entry.getValue().equals(category)) {
				String aliasName = dictAliases.get(entry.getKey());
				productsFromCategory.add(aliasName);
			}
		}
		
		
		for(YearDTO year: yearsArrayList) {
			curr_line = (ArrayList<String>) year.getTop10Headlines();
			for (int i = 0; i < curr_line.size() ; i++) {
				for(String name: productsFromCategory) {
					if(curr_line.get(i).contains(name)) {
						catHighlightArrayList.add(new CategoryHighlightDTO(year.getYear(),name,curr_line.get(i)));						
					}
				}	
			}	
		}
		return catHighlightArrayList;
	}
	
	
	//---------------------------------------------------
	/*
	 * Private Helpful methods to find the Minimum and Maximum
	 * Measurement from a list of MeasurementDTO items
	 * 
	 */
	@SuppressWarnings("unused")
	private double findMinElement(List<MeasurementDTO> list) {
		double min = list.get(0).getValue();
		for(MeasurementDTO m: list) {
			if (m.getValue() < min) {
				min = m.getValue();
			}
		}
		return min;
	}
	
	@SuppressWarnings("unused")
	private double findMaxElement(List<MeasurementDTO> list) {
		double max = list.get(0).getValue();
		for(MeasurementDTO m: list) {
			if (m.getValue() > max) {
				max = m.getValue();
			}
		}
		return max;
	}
	
	//--------------------------------------------------
	
	@Override
	public List<ProductStatsDTO> computeProductStats() {
		ArrayList<ProductStatsDTO> statsArrayList = new ArrayList<ProductStatsDTO>();
		ArrayList<MeasurementDTO> values;
		
		double min;
		double average;
		double max;
		double lastValue;
		
		for(ProductDTO product: productsArrayList) {
			values = (ArrayList<MeasurementDTO>) product.getMeasurements();
			min = max = average = lastValue = values.get(0).getValue();
			String name = product.getName();
			double sum = 0;
			
			for(int i = 1; i < values.size(); i++) {
				
				if (values.get(i).getValue() < min) {
					min = values.get(i).getValue();
				}
				
				if (values.get(i).getValue() > max) {
					max = values.get(i).getValue();
				}
				
				sum += values.get(i).getValue();
			}
			
			average = sum / (yearsArrayList.size());
			lastValue = values.get(values.size()-1).getValue();
			
			statsArrayList.add(new ProductStatsDTO(name, min, average,max,lastValue));
		}
		return statsArrayList;
	}

	
	public List<Top10AppearanceDTO> computeStats(HashMap<String,Integer> map){
		List<Top10AppearanceDTO> top10ArrayList = new ArrayList<Top10AppearanceDTO>();
		List<String> top10Aliases;
		
		for(YearDTO year: yearsArrayList) {
			top10Aliases = year.getTop10Aliases();
		}
		
		for(YearDTO year: yearsArrayList) {
			top10Aliases = year.getTop10Aliases();
			for(String product: top10Aliases) {
				if(map.containsKey(product)) {
					int count = map.get(product);
					count++;
					map.put(product, count);
				}
			}
		}
		
		for(Map.Entry<String,Integer> entry: map.entrySet()) {
			top10ArrayList.add(new Top10AppearanceDTO(entry.getKey(),entry.getValue()));
		}
		return top10ArrayList;
	}
	
	
	@Override
	public List<Top10AppearanceDTO> computeTop10ProductAppearances() {
		ArrayList<Top10AppearanceDTO> top10ArrayList = new ArrayList<Top10AppearanceDTO>();
		HashMap<String, Integer> productTop10HashMap = new HashMap<String,Integer>();
		List<String> top10Aliases;
		
		//Initialiaze the dictionary
		for(Map.Entry<String, String> entry : dictAliases.entrySet()) {
			productTop10HashMap.put(entry.getValue(),0);
		}
		
		for(YearDTO year: yearsArrayList) {
			top10Aliases = year.getTop10Aliases();
			for(String product: top10Aliases) {
				if(productTop10HashMap.containsKey(product)) {
					int count = productTop10HashMap.get(product);
					count++;
					productTop10HashMap.put(product, count);
				}
			}
		}
		
		for(Map.Entry<String,Integer> entry: productTop10HashMap.entrySet()) {
			top10ArrayList.add(new Top10AppearanceDTO(entry.getKey(),entry.getValue()));
		}
		top10ArrayList.sort(Comparator.comparing(Top10AppearanceDTO::getCount).reversed());
		return top10ArrayList;
	}

	@Override
	public List<Top10AppearanceDTO> computeTop10CategoryAppearances() {

	    ArrayList<Top10AppearanceDTO> top10ArrayList = new ArrayList<>();
	    HashMap<String,Integer> categoryTop10HashMap = new HashMap<>();
	    List<String> top10Aliases;

	    // Initialize dictionary: category → count
	    for (String category : dictCategories.values()) {
	        categoryTop10HashMap.putIfAbsent(category, 0);
	    }

	    // Count appearances per category
	    for (YearDTO year : yearsArrayList) {
	        top10Aliases = year.getTop10Aliases();
	        for (String alias : top10Aliases) {

	            String category = dictCategories.get(alias);   // get category FROM alias
	            if (category != null) {
	                int count = categoryTop10HashMap.get(category);
	                categoryTop10HashMap.put(category, count + 1);
	            }
	        }
	    }

	    // Convert to list
	    for(Map.Entry<String,Integer> entry: categoryTop10HashMap.entrySet()) {
	        top10ArrayList.add(new Top10AppearanceDTO(entry.getKey(), entry.getValue()));
	    }

	    // Sort descending
	    top10ArrayList.sort(Comparator.comparing(Top10AppearanceDTO::getCount).reversed());

	    return top10ArrayList;
	}


	@Override
	public List<YearDTO> reportAllYearsAllProductPrices() {
		return yearsArrayList;
	}
	
}