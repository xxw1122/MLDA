package mldc.metricNames;

/**
* Class storing the labels relation metrics names
*
* @author Jose Maria Moyano Murillo
*/
public class LabelsRelationMetrics {
	
	/**
	 * Get the names of the available labels relation metrics
	 * 
	 * @return An array with the names
	 */
	public static String[] getAvailableMetrics(){
		String [] metrics = new String[21];
		
		metrics[0] = "Average examples per labelset";
		metrics[1] = "Average of unconditionally dependent label pairs by chi-square test";
		metrics[2] = "Bound";
		metrics[3] = "Diversity";
		metrics[4] = "Number of labelsets up to 2 examples";
		metrics[5] = "Number of labelsets up to 5 examples";
		metrics[6] = "Number of labelsets up to 10 examples";
		metrics[7] = "Number of labelsets up to 50 examples";
		metrics[8] = "Number of labelsets up to N examples";
		metrics[9] = "Number of unconditionally dependent label pairs by chi-square test";
		metrics[10] = "Number of unique labelsets";
		metrics[11] = "Proportion of distinct labelsets";
		metrics[12] = "Ratio of labelsets with number of examples < half of the attributes";
		metrics[13] = "Ratio of unconditionally dependent label pairs by chi-square test";
		metrics[14] = "Ratio of number of labelsets up to 2 examples";
		metrics[15] = "Ratio of number of labelsets up to 5 examples";
		metrics[16] = "Ratio of number of labelsets up to 10 examples";
		metrics[17] = "Ratio of number of labelsets up to 50 examples";
		metrics[17] = "Ratio of number of labelsets up to N examples";
		metrics[19] = "SCUMBLE";
		metrics[20] = "Standard deviation of examples per labelset";
		
		return metrics;
	}

}