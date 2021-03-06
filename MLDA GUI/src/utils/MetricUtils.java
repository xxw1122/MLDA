/*
 * This file is part of the MLDA.
 *
 * (c)  Jose Maria Moyano Murillo
 *      Eva Lucrecia Gibaja Galindo
 *      Sebastian Ventura Soto <sventura@uco.es>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import mlda.base.*;
import mlda.metricNames.*;
import mlda.util.*;
import mlda.labelsRelation.*;
import mlda.dimensionality.*;
import mlda.labelsDistribution.*;
import mlda.attributes.*;
import mlda.imbalance.*;
import mulan.data.MultiLabelInstances;
import static utils.DataInfoUtils.getLabelByLabelname;
import static utils.Utils.existsValue;
import static utils.Utils.hasMoreNDigits;
import weka.core.Attribute;
import weka.core.Instances;
import static utils.Utils.getMax;

/**
 * Utils for metrics
 * 
 * @author Jose Maria Moyano Murillo
 */
public class MetricUtils {
    
    /**
     * Truncate value as String
     * 
     * @param value Value
     * @param digits Number of digits
     * @return Truncated value
     */
    public static String truncateValue (String value, int digits)
    {
        return truncateValue(Double.parseDouble(value), digits);
    }
    
    /**
     * Truncate value
     * 
     * @param value Value
     * @param digits Number of digits
     * @return Truncated value
     */
    public static String truncateValue(double value, int digits)
    {
        String number = Double.toString(value);
        int countDigits = 0;
        String result = "";
        boolean flag =false;
        
        if(!hasMoreNDigits(value, digits)) {
            return Double.toString(value);
        }
        
        for(int i=0; i<number.length();i++)
        {
            if(flag && countDigits!=digits){
                countDigits++;
            }
                
            if(number.charAt(i)=='.') {
                flag=true; 
                continue;
            }
            
            if(countDigits == digits) 
            {
                result=number.substring(0,i);
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Get maximum IR intra class
     * 
     * @param imbalancedData Imbalanced data
     * @param visited Visited
     * @return Imbalanced feature of max IR intra class
     */
    public static ImbalancedFeature getMaxIRIntraClass(ImbalancedFeature[] 
            imbalancedData, ArrayList<String> visited)
    {
        ImbalancedFeature max = null ;
         
        for(ImbalancedFeature current : imbalancedData)
        {
            if(! DataInfoUtils.existsAttribute(visited, current)) {
                if(max == null) {
                    max = current;
                }
                else
                {
                    if(max.getIRIntraClass() <= current.getIRIntraClass() && max.getVariance() < current.getVariance()) {
                        max = current;
                    }
                }
            }
        }
        
        return max;
    }
    
    /**
     * Get maximum IR inter class
     * 
     * @param imbalancedData Imbalanced data
     * @param visited Visited
     * @return Imbalanced feature of maximum IR inter class
     */
    public static ImbalancedFeature getMaxIRInterClass(ImbalancedFeature[] 
            imbalancedData, ArrayList<String> visited)
    {
        ImbalancedFeature max = null;
         
        for(ImbalancedFeature current : imbalancedData)
        {
            if(! DataInfoUtils.existsAttribute(visited, current)) {
                if(max == null) {
                    max = current;
                }
                else
                {
                    if(max.getIRInterClass()<= current.getIRInterClass()&& max.getVariance() < current.getVariance()) {
                        max = current;
                    }
                }
            }
        }
        
        return max;
    }
    
    /**
     * Get minimum IR
     * 
     * @param imbalancedData Imbalanced data
     * @param visited Visited
     * @return Imbalanced feature with minimum IR
     */
    public static ImbalancedFeature getMinIR(ImbalancedFeature[] imbalancedData, 
            ArrayList<String> visited)
    {
        ImbalancedFeature min = null ;
         
        for( ImbalancedFeature current : imbalancedData )
        {
            if(! DataInfoUtils.existsAttribute(visited, current)) {
                if(min == null) {
                    min = current;
                }
                else
                {
                    if(min.getIRIntraClass() >= current.getIRIntraClass() && min.getVariance() > current.getVariance()) {
                        min = current;
                    }
                }
            }
        }
        
        return min;
    }
    
    /**
     * Sort labels by IR intra class
     * 
     * @param imbalancedData Labels as ImbalancedFeature objects
     * @return Sorted labels
     */
    public static ImbalancedFeature[] sortImbalancedDataByIRIntraClass(
            ImbalancedFeature[] imbalancedData)
    {
        ImbalancedFeature[] sorted = new ImbalancedFeature[imbalancedData.length];
        
        ArrayList<String> visited = new ArrayList();
        ImbalancedFeature current;
        
        for(int i=0; i<imbalancedData.length; i++)
        {
            current = MetricUtils.getMaxIRIntraClass(imbalancedData,visited);
            if(current == null) {
                break;
            }
            
            sorted[i]=current;
            visited.add(current.getName());
        }
        
        return sorted;                
    }

    /**
     * Obtain labels ordered by IR inter class
     * 
     * @param dataset Dataset
     * @param labelsByFrequency Labels
     * @return Labels sorted by IR inter class
     */
    public static ImbalancedFeature[] getImbalancedDataByIRInterClass( 
            MultiLabelInstances dataset, ImbalancedFeature[] labelsByFrequency)
    {
        int[] labelIndices = dataset.getLabelIndices();
        
        ImbalancedFeature[] imbalancedData = new ImbalancedFeature[labelIndices.length];
         
        Instances instances = dataset.getDataSet();
         
        int n1=0, n0=0, maxAppearance;
        double is, IRIntraClass, variance, IRInterClass;         
        double mean = dataset.getNumInstances()/2;
         
        Attribute currentAttribute;
        ImbalancedFeature currentLabel;        
         
        for(int i=0; i<labelIndices.length;i++)
        {
            currentAttribute = instances.attribute(labelIndices[i]);
           
            for(int j=0; j<instances.size();j++)
            {
                is = instances.instance(j).value(currentAttribute);
                if(is == 1.0) {
                    n1++;
                }
                else {
                    n0++;
                }
            } 
            
            try { 
                if(n0 ==0 || n1 ==0) {
                    IRIntraClass = 0;
                }
                else if(n0>n1) {
                    IRIntraClass = n0/(n1*1.0);
                }
                else {
                    IRIntraClass = n1/(n0*1.0);
                }
            } catch(Exception e1)
            {
                e1.printStackTrace();
                IRIntraClass = 0;            
            }
                    
            variance = (Math.pow((n0-mean), 2) + Math.pow((n1-mean), 2))/2;
             
            currentLabel = getLabelByLabelname(currentAttribute.name(), labelsByFrequency);
             
            maxAppearance = labelsByFrequency[0].getAppearances();
             
            if(currentLabel.getAppearances() <= 0){
                IRInterClass = Double.NaN;
            }
            else{
                IRInterClass = maxAppearance/(currentLabel.getAppearances()*1.0);
            }

            imbalancedData[i] = new ImbalancedFeature(currentAttribute.name(),currentLabel.getAppearances(),IRIntraClass, variance, IRInterClass);
             
            n0 = 0;
            n1 = 0;
        }
         
        return imbalancedData;
    }
    
    /**
     * Obtain labels as ImbalancedFeature objects
     * 
     * @param dataset Datasets
     * @return Labels as ImbalanceFeature array
     */
    public static ImbalancedFeature[] getImbalancedData(
            MultiLabelInstances dataset)
    {
        int[] labelIndices = dataset.getLabelIndices();
        
        ImbalancedFeature[] imbalancedData = new ImbalancedFeature[labelIndices.length];
         
        Instances instances = dataset.getDataSet();
         
        int n1=0, n0=0;
        double is, IR, variance;         
        double mean = dataset.getNumInstances()/2;
         
        Attribute current;
         
        for(int i=0; i<labelIndices.length;i++)
        {
            current= instances.attribute(labelIndices[i]);
           
            for(int j=0; j<instances.size();j++)
            {
                is = instances.instance(j).value(current);
                if(is == 1.0) {
                    n1++;
                }
                else {
                    n0++;
                }
            } try { 
                if(n0 ==0 || n1 ==0) {
                    IR=0;
                }
                else if(n0>n1) {
                    IR= n0/(n1*1.0);
                }
                else {
                    IR=n1/(n0*1.0);
                }  
            } catch(Exception e1)
            {
                e1.printStackTrace();
                IR=0;            
            }
                    
            variance = (Math.pow((n0-mean), 2) + Math.pow((n1-mean), 2))/2;

            imbalancedData[i] = new ImbalancedFeature(current.name(), IR, variance);
             
            n0 = 0;
            n1 = 0;
        }
         
        return imbalancedData;
    }
    
    /**
     * Obtain labels ordered by number of appearances
     * 
     * @param dataset Dataset
     * @return Labels as ImbalanceFeature objects
     */
    public static ImbalancedFeature[] getImbalancedDataByAppearances(
            MultiLabelInstances dataset)
    {
        int[] labelIndices = dataset.getLabelIndices();
        
        ImbalancedFeature[] imbalancedData = new ImbalancedFeature[labelIndices.length];
         
        Instances instances = dataset.getDataSet();
         
        int appearances = 0;
        double is;
        Attribute current;
         
        for(int i=0; i<labelIndices.length;i++)
        {
            current = instances.attribute(labelIndices[i]);
             
            for(int j=0; j<instances.size();j++)
            {
                is = instances.instance(j).value(current);
                if(is ==1.0) {
                    appearances++;
                }
            }
            imbalancedData[i] = new ImbalancedFeature(current.name(), appearances);
            appearances = 0;
        }
         
        return imbalancedData;
    }
    
    /**
     * Sort labels by frequency
     * 
     * @param labelFrequency Labels as ImbalanceFeature array
     * @return Sorted labels
     */
    public static ImbalancedFeature[] sortByFrequency (ImbalancedFeature[] 
            labelFrequency)
    {
        ArrayList<ImbalancedFeature> list = new ArrayList();
        
        for(int i=0; i<labelFrequency.length; i++)
        {
            list.add(labelFrequency[i]);
        }
        
        ImbalancedFeature[] sorted = new ImbalancedFeature [labelFrequency.length];
        
        for(int i=0 ; i<labelFrequency.length; i++)
        {
            sorted[i] = getMax(list);
            list.remove(sorted[i]);
        }
        
        return sorted;
    }
    
    /**
     * Obtain number of labels with a certain value
     * 
     * @param imbalancedData Labels as ImbalancedFeature objects
     * @param visited Visited
     * @param current Current value
     * @return Number of labels
     */
    public static int getNumLabelsByIR(ImbalancedFeature[] imbalancedData, 
            double[] visited , double current)
    {
        if (existsValue(visited,current)) {
            return -1;
        }
        
        int appearances = 0;
        
        for(int i=0; i<imbalancedData.length;i++)
        {
            if(current > imbalancedData[i].getIRIntraClass()) {
                return appearances;
            }
            if(current == imbalancedData[i].getIRIntraClass()) {
                appearances++;
            }
        }
        
        return appearances;
    }
    
    /**
     * Obtain number of labels by a certain IR
     * 
     * @param IRInterClass IR inter class
     * @param visited Visited
     * @param current Current value
     * @return Number of labels
     */
    public static int getNumLabelsByIR(double[] IRInterClass, double[] visited, 
            double current)
    {
        if (existsValue(visited,current)) {
            return -1;
        }
        
        int appearances = 0;
        
        for(int i=0; i<IRInterClass.length;i++)
        {
            if(current == IRInterClass[i]) {
                appearances++;
            }
        }
        
        return appearances;
    }
    
    /**
     * Obtain metric value, given the name
     * 
     * @param metric Metric name
     * @param dataset Dataset
     * @return Metric value as String
     */
    public static String getMetricValue(String metric, MultiLabelInstances 
            dataset)
    {       
        double value = -1.0;
        
        MLDataMetric mldm = null;
        
        try{           
            
        switch (metric) 
        {
            case "Labels x instances x features":  
                mldm = new LxIxF();
                break;     

            case "Instances":
                mldm = new mlda.dimensionality.Instances();
                break;
            
            case "Attributes":
                mldm = new mlda.dimensionality.Attributes();
                break;
                
            case "Labels":
                mldm = new Labels();
                break;
            
            case "Label density":
                mldm = new Density();
                break;                

            case "Label Cardinality":
                mldm = new Cardinality();
                break;

            case "Distinct labelsets":
                mldm = new DistinctLabelsets();
                break;
            
            case "Number of unique labelsets":
                mldm = new UniqueLabelsets();
                break;
            
            case "Proportion of distinct labelsets":
                mldm = new ProportionDistinctLabelsets();
                break;
            
            case "Density":
                mldm = new Density();
                break;
            
            case "Cardinality":
                mldm = new Cardinality();
                break;
            
            case "Bound":
                mldm = new Bound();
                break;
            
            case "Diversity":
                mldm = new Diversity();
                break;
            
            case "Proportion of unique label combination (PUniq)":
                mldm = new PUniq();
                break;
            
            case "Proportion of maxim label combination (PMax)":
                mldm = new PMax();
                break;
            
            case "Ratio of number of instances to the number of attributes":
                mldm = new RatioInstancesToAttributes();
                break;
            
            case "Number of binary attributes":
                mldm = new BinaryAttributes();
                break;
            
            case "Proportion of binary attributes":
                mldm = new ProportionBinaryAttributes();
                break;
            
            case "Proportion of nominal attributes":
                mldm = new ProportionNominalAttributes();
                break;
            
            case "Proportion of numeric attributes":
                mldm = new ProportionNumericAttributes();
                break;
            
            case "Number of nominal attributes":
                mldm = new NominalAttributes();
                break;
            
            case "Number of numeric attributes":
                mldm = new NumericAttributes();
                break;

            case "Mean of mean of numeric attributes":
                mldm = new MeanOfMeanOfNumericAttributes();
                break;
            
            case "Mean of standard deviation of numeric attributes":
                mldm = new MeanStdvNumericAttributes();
                break;
            
            case "Mean of skewness of numeric attributes":
                mldm = new MeanSkewnessNumericAttributes();
                break;
            
            case "Mean of kurtosis":
                mldm = new MeanKurtosis();
                break;
            
            case "Mean of entropies of nominal attributes":
                mldm = new MeanEntropiesNominalAttributes();
                break;
            
            case "Average absolute correlation between numeric attributes":
                mldm = new AvgAbsoluteCorrelationBetweenNumericAttributes();
                break;
            
            case "Proportion of numeric attributes with outliers":
                mldm = new ProportionNumericAttributesWithOutliers();
                break;
            
            case "Average gain ratio":
                mldm = new AvgGainRatio();
                break;
            
            case "Standard deviation of label cardinality":
                mldm = new StdvCardinality();
                break;
            
            case "Skewness cardinality":
                mldm = new SkewnessCardinality();
                break;
            
            case "Kurtosis cardinality":
                mldm = new KurtosisCardinality();
                break;
            
            case "Number of unconditionally dependent label pairs by chi-square test":
                mldm = new NumUnconditionalDependentLabelPairsByChiSquare();
                break;
            
            case "Ratio of unconditionally dependent label pairs by chi-square test":
                mldm = new RatioUnconditionalDependentLabelPairsByChiSquare();
                break;
            
            case "Average of unconditionally dependent label pairs by chi-square test":
                mldm = new AvgUnconditionalDependentLabelPairsByChiSquare();
                break;
            
            case "Number of labelsets up to 2 examples":
                mldm = new LabelsetsUpTo2Examples();
                break;
            
            case "Number of labelsets up to 5 examples":
                mldm = new LabelsetsUpTo5Examples();
                break;
            
            case "Number of labelsets up to 10 examples":
                mldm = new LabelsetsUpTo10Examples();
                break;
            
            case "Number of labelsets up to 50 examples":
                mldm = new LabelsetsUpTo50Examples();
                break;
            
            case "Ratio of labelsets with number of examples < half of the attributes":
                mldm = new RatioLabelsetsWithExamplesLessThanHalfAttributes();
                break;
            
            case "Ratio of number of labelsets up to 2 examples":
                mldm = new RatioLabelsetsUpTo2Examples();
                break;
            
            case "Ratio of number of labelsets up to 5 examples":
                mldm = new RatioLabelsetsUpTo5Examples();
                break;
            
            case "Ratio of number of labelsets up to 10 examples":
                mldm = new RatioLabelsetsUpTo10Examples();
                break;
            
            case "Ratio of number of labelsets up to 50 examples":
                mldm = new RatioLabelsetsUpTo50Examples();
                break;
  
            case "Average examples per labelset":
                mldm = new AvgExamplesPerLabelset();
                break;
            
            case "Minimal entropy of labels":
                mldm = new MinEntropy();
                break;
            
            case "Maximal entropy of labels":
                mldm = new MaxEntropy();
                break;
            
            case "Mean of entropies of labels":
                mldm = new MeanEntropy();
                break;
            
            case "Standard deviation of examples per labelset":
                mldm = new StdvExamplesPerLabelset();
                break;
                
            case "Mean of IR per label intra class":
                mldm = new MeanIRIntraClass();
                break;
            
            case "Max IR per label intra class":
                mldm = new MaxIRIntraClass();
                break;
            
            case "Mean of IR per label inter class":
                mldm = new MeanIRInterClass();
                break;
            
            case "Max IR per label inter class":
                mldm = new MaxIRInterClass();
                break;
            
            case "Mean of IR per labelset":
                mldm = new MeanIRLabelset();
                break;
            
            case "Max IR per labelset":
                mldm = new MaxIRLabelset();
                break;
            
            case "Mean of standard deviation of IR per label intra class":
                mldm = new MeanStdvIRIntraClass();
                break;
            
            case "CVIR inter class":
                mldm = new CVIRInterClass();     
                break;
            
            case "SCUMBLE":
                mldm = new SCUMBLE();
                break;    
            
            default:  
                value = -1.0;
                break;    
            }
        
            if(mldm != null){
                value = mldm.calculate(dataset);
            }
            else{
                value = -1.0;
            }
        
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        if(Double.isNaN(value) || value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY){
            return("NaN");
        }
        else{
            return(Double.toString(value));
        }
    }
    
    /**
     * Obtain row data for metrics table principal
     * 
     * @return Matrix with row data
     */
    public static Object[][] getRowData()
    {
        ArrayList<String> metrics = getAllMetrics();
        
        Object rowData[][] = new Object[metrics.size()][3];
        
        for(int i=0; i<metrics.size(); i++){
            if(metrics.get(i).charAt(0) != '<'){
                rowData[i][0] = metrics.get(i);
                rowData[i][1] = "-";
                rowData[i][2]= Boolean.FALSE;
            }
            else{
                rowData[i][0] = metrics.get(i);
                rowData[i][1] = "";
                rowData[i][2] = Boolean.TRUE;
            }
        }
        
        return rowData;
    }
    
    /**
     * Obtain row data for metrics table multiple datasets
     * 
     * @return Matrix with row data
     */
    public static Object[][] getRowDataMulti()
    {
        ArrayList metrics = getMetricsMulti();
        
        Object rowData[][] = new Object[metrics.size()][2];
        
        for(int i=0; i<metrics.size(); i++){
            rowData[i][0] = metrics.get(i);
            rowData[i][1]= Boolean.FALSE;
        }
        
        return rowData;
    }
    
    /**
     * Obtain all metric names
     * 
     * @return List of metric names
     */
    public static ArrayList<String> getAllMetrics()
    {
        return(getAllMetricsAlphaSorted());
    }
    
    /**
     * Get all metrics names sorted by type
     * 
     * @return List of metric names
     */
    public static ArrayList<String> getAllMetricsTypeSorted()
    {
        ArrayList<String> result= new ArrayList();

        //result.add("<html><b>Size metrics</b></html>");
        result.add("Attributes");
        result.add("Instances");
        result.add("Labels");
        result.add("Distinct labelsets");
        result.add("Labels x instances x features");
        result.add("Ratio of number of instances to the number of attributes");

        //result.add("<html><b>Label distribution</b></html>");
        result.add("Cardinality");
        result.add("Density");
        result.add("Maximal entropy of labels");
        result.add("Mean of entropies of labels");
        result.add("Minimal entropy of labels");
        result.add("Standard deviation of label cardinality");

        //result.add("<html><b>Relationship among labels</b></html>");
        result.add("Average examples per labelset");
        result.add("Average of unconditionally dependent label pairs by chi-square test");
        result.add("Bound");
        result.add("Diversity");
        result.add("Number of labelsets up to 2 examples");
        result.add("Number of labelsets up to 5 examples");
        result.add("Number of labelsets up to 10 examples");
        result.add("Number of labelsets up to 50 examples");
        result.add("Number of unconditionally dependent label pairs by chi-square test");
        result.add("Number of unique labelsets");
        result.add("Proportion of distinct labelsets");
        result.add("Ratio of labelsets with number of examples < half of the attributes");
        result.add("Ratio of unconditionally dependent label pairs by chi-square test");
        result.add("Ratio of number of labelsets up to 2 examples");
        result.add("Ratio of number of labelsets up to 5 examples");
        result.add("Ratio of number of labelsets up to 10 examples");
        result.add("Ratio of number of labelsets up to 50 examples");
        result.add("SCUMBLE");
        result.add("Standard deviation of examples per labelset");

        //result.add("<html><b>Imbalance metrics</b></html>");
        result.add("CVIR inter class");
        result.add("Kurtosis cardinality");
        result.add("Max IR per label inter class");
        result.add("Max IR per label intra class");
        result.add("Max IR per labelset");
        result.add("Mean of IR per label inter class");
        result.add("Mean of IR per label intra class");       
        result.add("Mean of IR per labelset");       
        result.add("Mean of kurtosis");
        result.add("Mean of skewness of numeric attributes");
        result.add("Mean of standard deviation of IR per label intra class");
        result.add("Proportion of maxim label combination (PMax)");
        result.add("Proportion of unique label combination (PUniq)");
        result.add("Skewness cardinality");

        //result.add("<html><b>Attributes metrics</b></html>");
        result.add("Average absolute correlation between numeric attributes");
        result.add("Average gain ratio");
        result.add("Mean of entropies of nominal attributes");
        result.add("Mean of mean of numeric attributes");
        result.add("Mean of standard deviation of numeric attributes");
        result.add("Number of binary attributes");
        result.add("Number of nominal attributes");
        result.add("Number of numeric attributes");
        result.add("Proportion of binary attributes");
        result.add("Proportion of nominal attributes");
        result.add("Proportion of numeric attributes with outliers");

        return result;
    }
    
    /**
     * Obtain all metric names sorted by name
     * 
     * @return List of metric names
     */
    public static ArrayList<String> getAllMetricsAlphaSorted()
    {
        ArrayList<String> result= new ArrayList();

        result.add("Attributes");
        result.add("Average absolute correlation between numeric attributes");
        result.add("Average examples per labelset");
        result.add("Average gain ratio");
        result.add("Average of unconditionally dependent label pairs by chi-square test");
        result.add("Bound");
        result.add("Cardinality");
        result.add("CVIR inter class");
        result.add("Density");
        result.add("Distinct labelsets");
        result.add("Diversity");
        result.add("Instances");
        result.add("Kurtosis cardinality");
        result.add("Labels");
        result.add("Labels x instances x features");
        result.add("Max IR per label inter class");
        result.add("Max IR per label intra class");
        result.add("Max IR per labelset");
        result.add("Maximal entropy of labels");
        result.add("Mean of entropies of labels");
        result.add("Mean of entropies of nominal attributes");
        result.add("Mean of IR per label inter class");
        result.add("Mean of IR per label intra class");       
        result.add("Mean of IR per labelset");
        result.add("Mean of kurtosis");
        result.add("Mean of mean of numeric attributes");
        result.add("Mean of skewness of numeric attributes");
        result.add("Mean of standard deviation of IR per label intra class");
        result.add("Mean of standard deviation of numeric attributes");
        result.add("Minimal entropy of labels");
        result.add("Number of binary attributes");
        result.add("Number of labelsets up to 2 examples");
        result.add("Number of labelsets up to 5 examples");
        result.add("Number of labelsets up to 10 examples");
        result.add("Number of labelsets up to 50 examples");
        result.add("Number of nominal attributes");
        result.add("Number of numeric attributes");
        result.add("Number of unconditionally dependent label pairs by chi-square test");
        result.add("Number of unique labelsets");
        result.add("Proportion of binary attributes");
        result.add("Proportion of distinct labelsets");
        result.add("Proportion of maxim label combination (PMax)");
        result.add("Proportion of nominal attributes");
        result.add("Proportion of numeric attributes");
        result.add("Proportion of numeric attributes with outliers");
        result.add("Proportion of unique label combination (PUniq)");
        result.add("Ratio of labelsets with number of examples < half of the attributes");
        result.add("Ratio of number of instances to the number of attributes");
        result.add("Ratio of unconditionally dependent label pairs by chi-square test");
        result.add("Ratio of number of labelsets up to 2 examples");
        result.add("Ratio of number of labelsets up to 5 examples");
        result.add("Ratio of number of labelsets up to 10 examples");
        result.add("Ratio of number of labelsets up to 50 examples");
        result.add("SCUMBLE");
        result.add("Skewness cardinality");
        result.add("Standard deviation of examples per labelset");
        result.add("Standard deviation of label cardinality");

        return result;
    }
    
    /**
     * Get the tooltip for a specific metric
     * 
     * @param metric Metric name
     * @return Tooltip for the metric
     */
    public static String getMetricTooltip(String metric)
    {
        String tooltip;
        
        switch (metric) {
            case "Attributes":
                tooltip = "Number of attributes";
                break;
            case "Instances":
                tooltip = "Number of instances";
                break;
            case "Labels":
                tooltip = "Number of labels";
                break;
            case "Distinct labelsets":
                tooltip = "Number of distinct labelsets";
                break;
            case "Labels x instances x features":
                tooltip = "Number of labels * number of instances * number of attributes";
                break;
            case "Ratio of number of instances to the number of attributes":
                tooltip = "Number of instances / number of attributes";
                break;

            case "Cardinality":
                tooltip = "Average number of labels per instance";
                break;
            case "Density":
                tooltip = "Cardinality / number of labels";
                break;
            case "Maximal entropy of labels":
                tooltip = "Maximal uncertainty value among the labels";
                break;
            case "Mean of entropies of labels":
                tooltip = "Average uncertainty among the labels";
                break;
            case "Minimal entropy of labels":
                tooltip = "Minimal uncertainty value among the labels";
                break;
            case "Standard deviation of label cardinality":
                tooltip = "Standard deviation of the number of labels for each instance";
                break;

            case "Average examples per labelset":
                tooltip = "Average number of instances per labelset";
                break;
            case "Average of unconditionally dependent label pairs by chi-square test":
                tooltip = "Average of chi-square test values for each pair of labels";
                break;
            case "Bound":
                tooltip = "Maximum number of possible labelsets";
                break;
            case "Diversity":
                tooltip = "Ratio of labelsets existing in the dataset (distinct labelset / bound)";
                break;
            case "Number of labelsets up to 2 examples":
                tooltip = "Number of labelsets with number of instances less or equal to 2";
                break;
            case "Number of labelsets up to 5 examples":
                tooltip = "Number of labelsets with number of instances less or equal to 5";
                break;
            case "Number of labelsets up to 10 examples":
                tooltip = "Number of labelsets with number of instances less or equal to 10";
                break;
            case "Number of labelsets up to 50 examples":
                tooltip = "Number of labelsets with number of instances less or equal to 50";
                break;
            case "Number of unconditionally dependent label pairs by chi-square test":
                tooltip = "Number of pairs of labels that are unconditionally dependent by chi-square test";
                break;
            case "Number of unique labelsets":
                tooltip = "Number of labelsets with only one instance";
                break;
            case "Proportion of distinct labelsets":
                tooltip = "Number of distinct labelsets / number of instances";
                break;
            case "Ratio of labelsets with number of examples < half of the attributes":
                tooltip = "Ratio of labelsets with number of instances less than half ot the number of attributes";
                break;
            case "Ratio of unconditionally dependent label pairs by chi-square test":
                tooltip = "Ratio of pairs of labels that are unconditionally dependent by chi-square test, indicating the level of interdependencies among labels";
                break;
            case "Ratio of number of labelsets up to 2 examples":
                tooltip = "Ratio of labelsets with number of instances less or equal to 2";
                break;
            case "Ratio of number of labelsets up to 5 examples":
                tooltip = "Ratio of labelsets with number of instances less or equal to 5";
                break;
            case "Ratio of number of labelsets up to 10 examples":
                tooltip = "Ratio of labelsets with number of instances less or equal to 10";
                break;
            case "Ratio of number of labelsets up to 50 examples":
                tooltip = "Ratio of labelsets with number of instances less or equal to 50";
                break;
            case "SCUMBLE":
                tooltip = "Measures the concurrence level among frequent and infrequent labels";
                break;
            case "Standard deviation of examples per labelset":
                tooltip = "Standard deviation of number of instances per labelset";
                break;

            case "CVIR inter class":
                tooltip = "Coefficient of variation of the IR per label inter class";
                break;
            case "Kurtosis cardinality":
                tooltip = "Kurtosis of the label cardinality";
                break;
            case "Max IR per label inter class":
                tooltip = "Maximum value of IR per label inter class";
                break;
            case "Max IR per label intra class":
                tooltip = "Maximum value of IR per label intra class";
                break;
            case "Max IR per labelset":
                tooltip = "Maximum value of IR per labelset";
                break;
            case "Mean of IR per label inter class":
                tooltip = "Average value of IR per label inter class";
                break;
            case "Mean of IR per label intra class":
                tooltip = "Average value of IR per label intra class";
                break;       
            case "Mean of IR per labelset":
                tooltip = "Average value of IR per labelset";
                break;       
            case "Mean of kurtosis":
                tooltip = "Average value of kurtosis of all numeric attributes";
                break;
            case "Mean of skewness of numeric attributes":
                tooltip = "Average value of skewness of all numeric attributes";
                break;
            case "Mean of standard deviation of IR per label intra class":
                tooltip = "Average value of standard deviation of IR per label intra class values";
                break;
            case "Proportion of maxim label combination (PMax)":
                tooltip = "Proportion of instances associated with the most frequent labelset";
                break;
            case "Proportion of unique label combination (PUniq)":
                tooltip = "Proportion of instances associated with labelsets appearing only once";
                break;
            case "Skewness cardinality":
                tooltip = "Skewness of the label cardinality";
                break;

            case "Average absolute correlation between numeric attributes":
                tooltip = "Average of absolute correlation values between numeric attributes, indicating robustness to irrelevant attributes";
                break;
            case "Average gain ratio":
                tooltip = "The average information gain ratio is obtained by splitting the data according to each target attribute";
                break;
            case "Mean of entropies of nominal attributes":
                tooltip = "Average value of entropies of nominal attributes";
                break;
            case "Mean of mean of numeric attributes":
                tooltip = "Average of average values of all numeric attributes";
                break;
            case "Mean of standard deviation of numeric attributes":
                tooltip = "Average value of standard deviation of numeric attributes";
                break;
            case "Number of binary attributes":
                tooltip = "Number of binary attributes";
                break;
            case "Number of nominal attributes":
                tooltip = "Number of nominal attributes";
                break;
            case "Number of numeric attributes":
                tooltip = "Number of numeric attributes";
                break;
            case "Proportion of binary attributes":
                tooltip = "Proportion of attributes that are binary";
                break;
            case "Proportion of nominal attributes":
                tooltip = "Proportion of attributes that are nominal";
                break;
            case "Proportion of numeric attributes":
                tooltip = "Proportion of attributes that are numeric";
                break;
            case "Proportion of numeric attributes with outliers":
                tooltip = "Proportion of numeric attributes having outliers";
                break;

            default:
                tooltip = metric;
                break;
        }

        return(tooltip);
    }
    
    /**
     * Get list of metrics for multiple datasets tab
     * 
     * @return List of metric names
     */
    public static ArrayList<String> getMetricsMulti()
    {
        return(getAllMetrics());
    }
    
    /**
     * Obtain IR intra class values
     * 
     * @param imbalancedData Labels
     * @return IR values
     */
    public static double[] getIRIntraClassValues(ImbalancedFeature[] 
            imbalancedData)
    {
        double[] result = new double[imbalancedData.length];
        
        for(int i=0; i<imbalancedData.length; i++)
        {
            result[i] = imbalancedData[i].getIRIntraClass();
        }
        
        return result;
    }
    
    /**
     * Obtain IR inter class values
     * 
     * @param imbalancedData Labels as ImbalancedFeature array
     * @return Array with IR inter-class
     */
    public static double[] getIRInterClassValues(ImbalancedFeature[] 
            imbalancedData)
    {        
        ImbalancedFeature[] sortedImbalancedData = MetricUtils.sortByFrequency(imbalancedData);
        
        double[] IRInterClass = new double[imbalancedData.length];
        
        int max = sortedImbalancedData[0].getAppearances();
        double value;
        
        for(int i=0;i<sortedImbalancedData.length; i++)
        {
            if(sortedImbalancedData[i].getAppearances() <= 0){
                value = Double.NaN;
            }
            else{
                value = max/(sortedImbalancedData[i].getAppearances()*1.0);
            }
            
            IRInterClass[i] = value;
        }
        
        return IRInterClass;
    }
    
    /**
     * Get metric value formatted as String
     * 
     * @param name Metric name
     * @param value Metric value
     * @return Formatted value
     */
    public static String getValueFormatted(String name, String value){
        String formattedValue;

        value = value.replace(",", ".");

        if(value.equals("-")){
            return value;
        }

        if(value.equals("NaN")){
            return "---";
        }

        //Scientific notation numbers
        if( (((Math.abs(Double.parseDouble(value)*1000) < 1.0)) &&
                ((Math.abs(Double.parseDouble(value)*1000) > 0.0))) ||
                (Math.abs(Double.parseDouble(value)/1000.0) > 10)){
            NumberFormat formatter = new DecimalFormat("0.###E0");
            formattedValue = formatter.format(Double.parseDouble(value));
        }
        //Integer numbers
        else if( (name.toLowerCase().equals("attributes"))
                || (name.toLowerCase().equals("bound"))
                || (name.toLowerCase().equals("distinct labelsets"))
                || (name.toLowerCase().equals("instances"))
                || (name.toLowerCase().equals("labels x instances x features"))
                || (name.toLowerCase().equals("labels"))
                || (name.toLowerCase().equals("number of binary attributes"))
                || (name.toLowerCase().equals("number of labelsets up to 2 examples"))
                || (name.toLowerCase().equals("number of labelsets up to 5 examples"))
                || (name.toLowerCase().equals("number of labelsets up to 10 examples"))
                || (name.toLowerCase().equals("number of labelsets up to 50 examples"))
                || (name.toLowerCase().equals("number of nominal attributes"))
                || (name.toLowerCase().equals("number of numeric attributes"))
                || (name.toLowerCase().equals("number of unique labelsets"))
                || (name.toLowerCase().equals("number of unconditionally dependent label pairs by chi-square test"))){
            
            NumberFormat formatter = new DecimalFormat("#0");
            formattedValue = formatter.format(Double.parseDouble(value));
        }
        //Decimal numbers
        else{
            NumberFormat formatter = new DecimalFormat("#0.000");
            formattedValue = formatter.format(Double.parseDouble(value));
        }
        
        formattedValue = formattedValue.replace(",", ".");
        
        return formattedValue;
    }
    
    /**
     * Obtain metric value formatted to the specified number of decimal places
     * 
     * @param value Metric value
     * @param nDecimals Number of decimal places
     * @return Formatted value
     */
    public static String getValueFormatted(String value, int nDecimals){
        String formattedValue;

        value = value.replace(",", ".");

        if(value.equals("-")){
            return value;
        }

        if(value.equals("NaN")){
            return "---";
        }

        //Scientific notation numbers
        if( (((Math.abs(Double.parseDouble(value)*1000) < 1.0)) &&
                ((Math.abs(Double.parseDouble(value)*1000) > 0.0))) ||
                (Math.abs(Double.parseDouble(value)/1000.0) > 10)){
            NumberFormat formatter = new DecimalFormat("0.###E0");
            formattedValue = formatter.format(Double.parseDouble(value));
        }
        //Decimal numbers
        else{
            String f = "#0.";
            for(int i=0; i<nDecimals; i++){
                f += "0";
            }
            
            NumberFormat formatter = new DecimalFormat(f); 
            formattedValue = formatter.format(Double.parseDouble(value));
        } 

        formattedValue = formattedValue.replace(",", ".");
        
        return formattedValue;
    }

}
