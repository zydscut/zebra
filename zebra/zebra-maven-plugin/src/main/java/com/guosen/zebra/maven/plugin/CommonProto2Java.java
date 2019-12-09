package com.guosen.zebra.maven.plugin;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ProtocolStringList;
public class CommonProto2Java {


    private final String        discoveryRoot;

    private final String        generatePath;

    private final CommandProtoc commondProtoc;

    private Map<String, String> pojoTypes;

    private CommonProto2Java(String discoveryRoot, String generatePath, final File protocDependenciesPath){
        this.discoveryRoot = discoveryRoot;
        this.generatePath = generatePath;
        this.commondProtoc = CommandProtoc.configProtoPath(discoveryRoot, protocDependenciesPath);
    }

    public static CommonProto2Java forConfig(String discoveryRoot, String generatePath,
        final File protocDependenciesPath) {
        return new CommonProto2Java(discoveryRoot, generatePath, protocDependenciesPath);
    }

    public void generateFile(String protoPath) {
        try {
            if (pojoTypes == null) {
                pojoTypes = Maps.newHashMap();
            }
        } finally {
            if (!new File(protoPath).exists()) {
                System.out.println("[WARN]  protoPath:" + protoPath + " not exist, it may be in the third party jars, so it can't be generate");
                return;
            }
            FileDescriptorSet fileDescriptorSet = commondProtoc.invoke(protoPath);
            for (FileDescriptorProto fdp : fileDescriptorSet.getFileList()) {
                Pair<String, String> packageClassName = this.packageClassName(fdp.getOptions());
                if (packageClassName == null) {
                    continue;
                }
                ProtocolStringList dependencyList = fdp.getDependencyList();
                for (Iterator<String> it = dependencyList.iterator(); it.hasNext();) {
                    String dependencyPath = discoveryRoot + "/" + it.next();
                    generateFile(dependencyPath);
                }
                doPrint(fdp, packageClassName.getLeft(), packageClassName.getRight());
            }
        }
    }

    private Pair<String, String> packageClassName(FileOptions options) {
        String packageName = null;
        String className = null;
        for (Map.Entry<FieldDescriptor, Object> entry : options.getAllFields().entrySet()) {
            if (entry.getKey().getName().equals("java_package")) {
                packageName = entry.getValue().toString();
            }
            if (entry.getKey().getName().equals("java_outer_classname")) {
                className = entry.getValue().toString();
            }
        }
        if (packageName != null && className != null) {
            return new ImmutablePair<String, String>(packageName, className);
        }
        return null;
    }

    private void doPrint(FileDescriptorProto fdp, String javaPackage, String outerClassName) {
        List<DescriptorProto> messageDescList = fdp.getMessageTypeList();
        List<ServiceDescriptorProto> serviceDescList = fdp.getServiceList();
        List<EnumDescriptorProto> enumDescList = fdp.getEnumTypeList();
        printEnum(enumDescList, javaPackage, outerClassName);
        printMessage(messageDescList, javaPackage, outerClassName);
        printService(serviceDescList, javaPackage);
    }

    private void printService(List<ServiceDescriptorProto> serviceDescList, String javaPackage) {
        for (ServiceDescriptorProto serviceDesc : serviceDescList) {
            PrintServiceFile serviceFile = new PrintServiceFile(generatePath, javaPackage, serviceDesc.getName());
            try {
                serviceFile.setServiceMethods(serviceDesc.getMethodList());
                serviceFile.setPojoTypeCache(pojoTypes);
            } finally {
                serviceFile.print();
            }
        }
    }

    private void printMessage(List<DescriptorProto> messageDescList, String javaPackage, String outerClassName) {
        for (DescriptorProto messageDesc : messageDescList) {
            String pojoClassType = messageDesc.getName();
            String pojoPackageName = javaPackage + "." + outerClassName;
            String fullpojoType = pojoPackageName.toLowerCase() + "." + pojoClassType;
            pojoTypes.put(pojoClassType, fullpojoType);
            PrintMessageFile messageFile = new PrintMessageFile(generatePath, pojoPackageName, pojoClassType);
            try {
                messageFile.setMessageFields(messageDesc.getFieldList());
                messageFile.setPojoTypeCache(pojoTypes);
                messageFile.setSourceMessageDesc(messageDesc);
            } finally {
                messageFile.print();
            }
        }
    }

    private void printEnum(List<EnumDescriptorProto> enumDescList, String javaPackage, String outerClassName) {
        for (EnumDescriptorProto enumDesc : enumDescList) {
            String enumClassType = enumDesc.getName();
            String enumPackageName = javaPackage + "." + outerClassName;
            String fullpojoType = enumPackageName.toLowerCase() + "." + enumClassType;
            pojoTypes.put(enumClassType, fullpojoType);
            PrintEnumFile enumFile = new PrintEnumFile(generatePath, enumPackageName, enumClassType);
            try {
                enumFile.setEnumFields(enumDesc.getValueList());
            } finally {
                enumFile.print();
            }
        }
    }

}
