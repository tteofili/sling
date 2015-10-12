/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.distribution.serialization.impl.avro;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class AvroShallowResource extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"AvroShallowResource\",\"namespace\":\"org.apache.sling.distribution.serialization.impl.avro\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"valueMap\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"path\",\"type\":[\"string\",\"null\"]},{\"name\":\"resourceType\",\"type\":[\"string\",\"null\"]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence name;
  @Deprecated public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> valueMap;
  @Deprecated public java.lang.CharSequence path;
  @Deprecated public java.lang.CharSequence resourceType;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public AvroShallowResource() {}

  /**
   * All-args constructor.
   */
  public AvroShallowResource(java.lang.CharSequence name, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> valueMap, java.lang.CharSequence path, java.lang.CharSequence resourceType) {
    this.name = name;
    this.valueMap = valueMap;
    this.path = path;
    this.resourceType = resourceType;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return name;
    case 1: return valueMap;
    case 2: return path;
    case 3: return resourceType;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: name = (java.lang.CharSequence)value$; break;
    case 1: valueMap = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>)value$; break;
    case 2: path = (java.lang.CharSequence)value$; break;
    case 3: resourceType = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'name' field.
   */
  public java.lang.CharSequence getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
  }

  /**
   * Gets the value of the 'valueMap' field.
   */
  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getValueMap() {
    return valueMap;
  }

  /**
   * Sets the value of the 'valueMap' field.
   * @param value the value to set.
   */
  public void setValueMap(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
    this.valueMap = value;
  }

  /**
   * Gets the value of the 'path' field.
   */
  public java.lang.CharSequence getPath() {
    return path;
  }

  /**
   * Sets the value of the 'path' field.
   * @param value the value to set.
   */
  public void setPath(java.lang.CharSequence value) {
    this.path = value;
  }

  /**
   * Gets the value of the 'resourceType' field.
   */
  public java.lang.CharSequence getResourceType() {
    return resourceType;
  }

  /**
   * Sets the value of the 'resourceType' field.
   * @param value the value to set.
   */
  public void setResourceType(java.lang.CharSequence value) {
    this.resourceType = value;
  }

  /** Creates a new AvroShallowResource RecordBuilder */
  public static org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder newBuilder() {
    return new org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder();
  }
  
  /** Creates a new AvroShallowResource RecordBuilder by copying an existing Builder */
  public static org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder newBuilder(org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder other) {
    return new org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder(other);
  }
  
  /** Creates a new AvroShallowResource RecordBuilder by copying an existing AvroShallowResource instance */
  public static org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder newBuilder(org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource other) {
    return new org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder(other);
  }
  
  /**
   * RecordBuilder for AvroShallowResource instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<AvroShallowResource>
    implements org.apache.avro.data.RecordBuilder<AvroShallowResource> {

    private java.lang.CharSequence name;
    private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> valueMap;
    private java.lang.CharSequence path;
    private java.lang.CharSequence resourceType;

    /** Creates a new Builder */
    private Builder() {
      super(org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.name)) {
        this.name = data().deepCopy(fields()[0].schema(), other.name);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.valueMap)) {
        this.valueMap = data().deepCopy(fields()[1].schema(), other.valueMap);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.path)) {
        this.path = data().deepCopy(fields()[2].schema(), other.path);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.resourceType)) {
        this.resourceType = data().deepCopy(fields()[3].schema(), other.resourceType);
        fieldSetFlags()[3] = true;
      }
    }
    
    /** Creates a Builder by copying an existing AvroShallowResource instance */
    private Builder(org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource other) {
            super(org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.SCHEMA$);
      if (isValidValue(fields()[0], other.name)) {
        this.name = data().deepCopy(fields()[0].schema(), other.name);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.valueMap)) {
        this.valueMap = data().deepCopy(fields()[1].schema(), other.valueMap);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.path)) {
        this.path = data().deepCopy(fields()[2].schema(), other.path);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.resourceType)) {
        this.resourceType = data().deepCopy(fields()[3].schema(), other.resourceType);
        fieldSetFlags()[3] = true;
      }
    }

    /** Gets the value of the 'name' field */
    public java.lang.CharSequence getName() {
      return name;
    }
    
    /** Sets the value of the 'name' field */
    public org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder setName(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.name = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'name' field has been set */
    public boolean hasName() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'name' field */
    public org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder clearName() {
      name = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'valueMap' field */
    public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getValueMap() {
      return valueMap;
    }
    
    /** Sets the value of the 'valueMap' field */
    public org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder setValueMap(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
      validate(fields()[1], value);
      this.valueMap = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'valueMap' field has been set */
    public boolean hasValueMap() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'valueMap' field */
    public org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder clearValueMap() {
      valueMap = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'path' field */
    public java.lang.CharSequence getPath() {
      return path;
    }
    
    /** Sets the value of the 'path' field */
    public org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder setPath(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.path = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'path' field has been set */
    public boolean hasPath() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'path' field */
    public org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder clearPath() {
      path = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'resourceType' field */
    public java.lang.CharSequence getResourceType() {
      return resourceType;
    }
    
    /** Sets the value of the 'resourceType' field */
    public org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder setResourceType(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.resourceType = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'resourceType' field has been set */
    public boolean hasResourceType() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'resourceType' field */
    public org.apache.sling.distribution.serialization.impl.avro.AvroShallowResource.Builder clearResourceType() {
      resourceType = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    public AvroShallowResource build() {
      try {
        AvroShallowResource record = new AvroShallowResource();
        record.name = fieldSetFlags()[0] ? this.name : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.valueMap = fieldSetFlags()[1] ? this.valueMap : (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) defaultValue(fields()[1]);
        record.path = fieldSetFlags()[2] ? this.path : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.resourceType = fieldSetFlags()[3] ? this.resourceType : (java.lang.CharSequence) defaultValue(fields()[3]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
