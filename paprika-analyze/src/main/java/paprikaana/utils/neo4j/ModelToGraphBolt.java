
/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package paprikaana.utils.neo4j;

import paprikaana.entities.*;
import paprikaana.metrics.Metric;
import paprikaana.application.PaprikaAnalyzeMain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;



/**
 * Created by Geoffrey Hecht on 05/06/14.
 * Modified by Guillaume Willefert on 04/14/17
 */

public class ModelToGraphBolt {
	
	private Graph graph;
	private Session session;

    private static final String CLASSLABEL = "Class";
    private static final String EXTERNALCLASSLABEL ="ExternalClass";
    private static final String METHODLABEL = "Method";
    private static final String EXTERNALMETHODLABEL ="ExternalMethod";
    private static final String VARIABLELABEL ="Variable";
    private static final String ARGUMENTLABEL = "Argument";
    private static final String EXTERNALARGUMENTLABEL = "ExternalArgument";
    private static final String MODIFIED = "modifier";

    private Map<Entity,LowNode> methodNodeMap;
    private Map<PaprikaClass,LowNode> classNodeMap;
    private Map<PaprikaVariable,LowNode> variableNodeMap;

    private long key;

    public ModelToGraphBolt(){

		graph = new Graph();
		session = PaprikaAnalyzeMain.getSession();
        methodNodeMap = new HashMap<>();
        classNodeMap = new HashMap<>();
        variableNodeMap = new HashMap<>();
    }

    public LowNode insertApp(PaprikaApp paprikaApp,LowNode nodeVer){
        this.key =nodeVer.getID();
        LowNode appNode= new LowNode(PaprikaKeyWords.LABELAPP);
		StatementResult result;

  
        try ( Transaction tx = this.session.beginTransaction()){
        	appNode.addParameter(PaprikaKeyWords.APPKEY,key);
        	appNode.addParameter("name",paprikaApp.getName());
        	appNode.addParameter("category",paprikaApp.getCategory());
        	appNode.addParameter("package",paprikaApp.getPack());
        	appNode.addParameter("developer",paprikaApp.getDeveloper());
        	appNode.addParameter("rating",paprikaApp.getRating());
        	appNode.addParameter("nb_download",paprikaApp.getNbDownload());
        	appNode.addParameter("date_download",paprikaApp.getDate());
        	appNode.addParameter("version_code",paprikaApp.getVersionCode());
        	appNode.addParameter("version_name",paprikaApp.getVersionName());
        	appNode.addParameter("sdk",paprikaApp.getSdkVersion());
        	appNode.addParameter("target_sdk",paprikaApp.getTargetSdkVersion());

            Date date = new Date();
            SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.S");
            appNode.addParameter("date_analysis",  simpleFormat.format(date));
        	appNode.addParameter("size",paprikaApp.getSize());
        	appNode.addParameter("price", paprikaApp.getPrice());
           	
   
           	result=tx.run(graph.create(appNode));
    		long id=this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
    		appNode.setId(id);
    		
    		
			LowNode nodeVerSet= new LowNode(nodeVer.getLabel());
			long idi=nodeVer.getID();
			if(idi==-1) 
			nodeVerSet.setId(idi);
			
    		PaprikaAnalyzeMain.LOGGER.trace("etape 1");
            for(PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()){
            	PaprikaAnalyzeMain.LOGGER.trace(paprikaClass.getName());
            	tx.run(graph.relation(appNode,insertClass(paprikaClass,tx),RelationTypes.APP_OWNS_CLASS.name()));
            }
			nodeVerSet.addParameter(PaprikaKeyWords.ANALYSEINLOAD, "60");
			tx.run(graph.set(nodeVer, nodeVerSet));
			
        	PaprikaAnalyzeMain.LOGGER.trace("etape 2");
            for(PaprikaExternalClass paprikaExternalClass : paprikaApp.getPaprikaExternalClasses()){
            	PaprikaAnalyzeMain.LOGGER.trace(paprikaExternalClass.getName());
                insertExternalClass(paprikaExternalClass,tx);
            }
			nodeVerSet.addParameter(PaprikaKeyWords.ANALYSEINLOAD, "80");
			tx.run(graph.set(nodeVer, nodeVerSet));
            
        	PaprikaAnalyzeMain.LOGGER.trace("etape 3");
            for(Metric metric : paprikaApp.getMetrics()){
            	PaprikaAnalyzeMain.LOGGER.trace(metric.getName());
                insertMetric(metric, appNode);
            }
            
			nodeVerSet.addParameter(PaprikaKeyWords.ANALYSEINLOAD, "90");
			tx.run(graph.set(nodeVer, nodeVerSet));
            
            /*
             * On applique les nouvelles modifications au noeud
             */
            LowNode rawNode=new LowNode(PaprikaKeyWords.LABELAPP);
            rawNode.setId(id);
            tx.run(graph.set(rawNode, appNode));
        	nodeVerSet.addParameter(PaprikaKeyWords.ANALYSEINLOAD, "95");
            
            tx.success();
            
        }
        
        try ( Transaction tx = this.session.beginTransaction() ){
            createHierarchy(paprikaApp,tx);
            createCallGraph(paprikaApp,tx);
            tx.success();
        }
        
        session.close();
        return appNode;
    }

    private void insertMetric(Metric metric, LowNode node) {
        node.addParameter(metric.getName(), metric.getValue());
    }


    public LowNode insertClass(PaprikaClass paprikaClass,Transaction tx){
        LowNode classNode = new LowNode(CLASSLABEL);
        classNodeMap.put(paprikaClass,classNode);
        classNode.addParameter(PaprikaKeyWords.APPKEY, key);
        classNode.addParameter("name", paprikaClass.getName());
        classNode.addParameter(MODIFIED,  paprikaClass.getModifier().toString().toLowerCase());
        
        if(paprikaClass.getParentName() != null){
        	  classNode.addParameter("parent_name",  paprikaClass.getParentName());
        }

       	StatementResult result=tx.run(graph.create(classNode));
		long id=this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
		classNode.setId(id);

        for(PaprikaVariable paprikaVariable : paprikaClass.getPaprikaVariables()){
        	tx.run(graph.relation(classNode,insertVariable(paprikaVariable,tx),RelationTypes.CLASS_OWNS_VARIABLE.name()));
         
        }
        for(PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()){
        	tx.run(graph.relation(classNode,insertMethod(paprikaMethod,tx),RelationTypes.CLASS_OWNS_METHOD.name()));

        }
        
        for(Metric metric : paprikaClass.getMetrics()){
            insertMetric(metric,classNode);
        }
        
        LowNode rawNode=new LowNode(CLASSLABEL);
        rawNode.setId(id);
        tx.run(graph.set(rawNode, classNode));

        
        return classNode;
    }

    public LowNode insertExternalClass(PaprikaExternalClass paprikaClass,Transaction tx){
     
        LowNode classNode = new LowNode(EXTERNALCLASSLABEL);
        classNode.addParameter(PaprikaKeyWords.APPKEY, this.key);
        classNode.addParameter("name", paprikaClass.getName());
        if(paprikaClass.getParentName() != null){
            classNode.addParameter("parent_name",  paprikaClass.getParentName());
        }
    	StatementResult result=tx.run(graph.create(classNode));
		long id=this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
		classNode.setId(id);
        
        
        for(PaprikaExternalMethod paprikaExternalMethod : paprikaClass.getPaprikaExternalMethods()){
               	tx.run(graph.relation(classNode,insertExternalMethod(paprikaExternalMethod,tx),RelationTypes.CLASS_OWNS_METHOD.name()));
        }
        
        for(Metric metric : paprikaClass.getMetrics()){
            insertMetric(metric,classNode);
        }
        LowNode rawNode=new LowNode(EXTERNALCLASSLABEL);
        rawNode.setId(id);
        tx.run(graph.set(rawNode, classNode));
		
        return classNode;
    }

    public LowNode insertVariable(PaprikaVariable paprikaVariable,Transaction tx){
        LowNode variableNode = new LowNode(VARIABLELABEL);

        variableNodeMap.put(paprikaVariable,variableNode);
        variableNode.addParameter(PaprikaKeyWords.APPKEY, key);
        variableNode.addParameter("name", paprikaVariable.getName());
        variableNode.addParameter(MODIFIED,  paprikaVariable.getModifier().toString().toLowerCase());
        variableNode.addParameter("type", paprikaVariable.getType());
        
      	StatementResult result=tx.run(graph.create(variableNode));
    	long id=this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
    	variableNode.setId(id);


        for(Metric metric : paprikaVariable.getMetrics()){
            insertMetric(metric, variableNode);
        }
        LowNode rawNode=new LowNode(VARIABLELABEL);
        rawNode.setId(id);
        tx.run(graph.set(rawNode, variableNode));
        
        
        return variableNode;
    }
    
    public LowNode insertMethod(PaprikaMethod paprikaMethod,Transaction tx){
        LowNode methodNode = new LowNode(METHODLABEL);
        methodNodeMap.put(paprikaMethod,methodNode);
        
        methodNode.addParameter(PaprikaKeyWords.APPKEY, key);
        methodNode.addParameter("name", paprikaMethod.getName());
        methodNode.addParameter(MODIFIED,  paprikaMethod.getModifier().toString().toLowerCase());
        methodNode.addParameter("full_name", paprikaMethod.toString());
        methodNode.addParameter("return_type", paprikaMethod.getReturnType());
        

        
        for(Metric metric : paprikaMethod.getMetrics()){
            insertMetric(metric, methodNode);
        }
      	StatementResult result=tx.run(graph.create(methodNode));
    	long id=this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
    	methodNode.setId(id);
 
        for(PaprikaVariable paprikaVariable : paprikaMethod.getUsedVariables()){
            tx.run(graph.relation(methodNode,variableNodeMap.get(paprikaVariable),RelationTypes.USES.name()));
        }
        for(PaprikaArgument arg : paprikaMethod.getArguments()){
           	tx.run(graph.relation(methodNode,insertArgument(arg,tx),RelationTypes.METHOD_OWNS_ARGUMENT.name()));
        }
   
        return methodNode;
    }

    public LowNode insertExternalMethod(PaprikaExternalMethod paprikaMethod,Transaction tx){
        LowNode methodNode = new LowNode(EXTERNALMETHODLABEL);

        methodNodeMap.put(paprikaMethod,methodNode);
        
        methodNode.addParameter(PaprikaKeyWords.APPKEY, key);
        methodNode.addParameter("name", paprikaMethod.getName());
        methodNode.addParameter("full_name", paprikaMethod.toString());
        methodNode.addParameter("return_type", paprikaMethod.getReturnType());

        
        
        for(Metric metric : paprikaMethod.getMetrics()){
            insertMetric(metric, methodNode);
        }
        
      	StatementResult result=tx.run(graph.create(methodNode));
    	long id=this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
    	methodNode.setId(id); 
    	
        for(PaprikaExternalArgument arg : paprikaMethod.getPaprikaExternalArguments()){
         	tx.run(graph.relation(methodNode,insertExternalArgument(arg,tx),RelationTypes.METHOD_OWNS_ARGUMENT.name()));
        }
  
        return methodNode;
    }

    public LowNode insertArgument(PaprikaArgument paprikaArgument,Transaction tx){
        LowNode argNode = new LowNode(ARGUMENTLABEL);

        argNode.addParameter(PaprikaKeyWords.APPKEY, key);
        argNode.addParameter("name",paprikaArgument.getName());
        argNode.addParameter("position",paprikaArgument.getPosition());
        
      	StatementResult result=tx.run(graph.create(argNode));
    	long id=this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
    	argNode.setId(id);

        return argNode;
    }

    public LowNode insertExternalArgument(PaprikaExternalArgument paprikaExternalArgument,Transaction tx){
        LowNode argNode = new LowNode(EXTERNALARGUMENTLABEL);

        argNode.addParameter(PaprikaKeyWords.APPKEY, key);
        argNode.addParameter("name", paprikaExternalArgument.getName());
        argNode.addParameter("position",paprikaExternalArgument.getPosition());
        
  

        for(Metric metric : paprikaExternalArgument.getMetrics()){
            insertMetric(metric, argNode);
        }
        
    	StatementResult result=tx.run(graph.create(argNode));
    	long id=this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
    	argNode.setId(id);
        
        return argNode;
    }

    public void createHierarchy(PaprikaApp paprikaApp,Transaction tx) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            PaprikaClass parent = paprikaClass.getParent();
            
            if (parent != null) {
            	tx.run(graph.relation(classNodeMap.get(paprikaClass),classNodeMap.get(parent),RelationTypes.EXTENDS.name()));
    
            }
            for(PaprikaClass pInterface : paprikaClass.getInterfaces()){
            	tx.run(graph.relation(classNodeMap.get(paprikaClass),classNodeMap.get(pInterface),RelationTypes.IMPLEMENTS.name()));
            }
            
        }
    }

    public void createCallGraph(PaprikaApp paprikaApp,Transaction tx) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()){
                for(Entity calledMethod : paprikaMethod.getCalledMethods()){
                	tx.run(graph.relation(methodNodeMap.get(paprikaMethod),methodNodeMap.get(calledMethod),RelationTypes.CALLS.name()));
                }
            }
        }
    }
    
}
