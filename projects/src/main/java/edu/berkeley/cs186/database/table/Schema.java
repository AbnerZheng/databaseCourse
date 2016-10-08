package edu.berkeley.cs186.database.table;

import edu.berkeley.cs186.database.datatypes.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Schema of a particular table.
 *
 * Properties:
 * `fields`: an ordered list of column names
 * `fieldTypes`: an ordered list of data types corresponding to the columns
 * `size`: physical size (in bytes) of a record conforming to this schema
 */
public class Schema {
  private List<String> fields;
  private List<DataType> fieldTypes;
  private int size;

  public Schema(List<String> fields, List<DataType> fieldTypes) {
    assert(fields.size() == fieldTypes.size());

    this.fields = fields;
    this.fieldTypes = fieldTypes;
    this.size = 0;

    for (DataType dt : fieldTypes) {
      this.size += dt.getSize();
    }
  }

  /**
   * Verifies that a list of DataTypes corresponds to this schema. A list of
   * DataTypes corresponds to this schema if the number of DataTypes in the
   * list equals the number of columns in this schema, and if each DataType has
   * the same type and size as the columns in this schema.
   *
   * @param values the list of values to check
   * @return a new Record with the DataTypes specified
   * @throws SchemaException if the values specified don't conform to this Schema
   */
  public Record verify(List<DataType> values) throws SchemaException {
    if(values.size() != fieldTypes.size()){
      throw  new SchemaException("Values specified don't conform to the schema");
    };
    for (int i = 0; i<values.size(); i++){
      DataType value = values.get(i);
      DataType field = fieldTypes.get(i);
      if(value.type() != field.type() || value.getSize() != field.getSize()){
        throw  new SchemaException("Values specified don't conform to the schema");
      }
    }
    return new Record(values);
  }

  /**
   * Serializes the provided record into a byte[]. Uses the DataTypes's
   * serialization methods. A serialized record is represented as the
   * concatenation of each serialized DataType. This method assumes that the
   * input record corresponds to this schema.
   *
   * @param record the record to encode
   * @return the encoded record as a byte[]
   */
  public byte[] encode(Record record) {
    List<DataType> values = record.getValues();

    // described in 9.7.1
    // fixed length record

    ByteBuffer result = ByteBuffer.allocate(size);
    DataType temp;
    for (DataType value: values){
      switch (value.type()){
        case INT:
          result.putInt(value.getInt());
          break;
        case FLOAT:
          result.putFloat(value.getFloat());
          break;
        case BOOL:
          result.put((byte) (value.getBool()?0x1:0x0));
          break;
        default:
          result.put(value.getString().getBytes());
      }
    }
    return result.array();
  }

  /**
   * Takes a byte[] and decodes it into a Record. This method assumes that the
   * input byte[] represents a record that corresponds to this schema.
   *
   * @param input the byte array to decode
   * @return the decoded Record
   */
  public Record decode(byte[] input) {
    List<DataType> result = new ArrayList<DataType>();
    ByteBuffer byteBuffer = ByteBuffer.allocate(input.length);
    byteBuffer.put(input);
    byteBuffer.flip();
    DataType temp;
    for (DataType dataType: fieldTypes){
      switch (dataType.type()){
        case INT:
          temp = new IntDataType(byteBuffer.getInt());
          break;
        case FLOAT:
          temp = new FloatDataType(byteBuffer.getFloat());
          break;
        case BOOL:
          byte a = byteBuffer.get();
          Boolean type;
          if(a == 0x1){
            type = true;
          }else{
            type = false;
          }
          temp = new BoolDataType(type);
          break;
        default:
          int s = dataType.getSize();
          byte[] dst = new byte[s];
          byteBuffer.get(dst);
          temp = new StringDataType(dst);
      }
      result.add(temp);
    }
    return new Record(result);
  }

  public int getEntrySize() {
    return this.size;
  }

  public List<String> getFieldNames() {
    return this.fields;
  }

  public List<DataType> getFieldTypes() {
    return this.fieldTypes;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Schema)) {
      return false;
    }

    Schema otherSchema = (Schema) other;

    if (this.fields.size() != otherSchema.fields.size()) {
      return false;
    }

    for (int i = 0; i < this.fields.size(); i++) {
      DataType thisType = this.fieldTypes.get(i);
      DataType otherType = this.fieldTypes.get(i);

      if (thisType.type() != otherType.type()) {
        return false;
      }

      if (thisType.equals(DataType.Types.STRING) && thisType.getSize() != otherType.getSize()) {
        return false;
      }
    }

    return true;
  }
}
