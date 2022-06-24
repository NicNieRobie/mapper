# JSON Mapper Library

Implementation of the `Mapper` interface that uses JSON as a marshalling format.

Author: *Pendishchuk Vladislav*

---

## Supported types
* Types marked as `Exported`:
  * Non-generic classes with a public parameterless constructor:
    * Non-inner classes;
    * Static inner classes;
  * Records.
* Numerical primitives and their wrappers;
* `Boolean` wrapper and `boolean` primitive;
* `Character` wrapper and `char` primitive;
* `String`;
* Enumerations;
* Lists;
* Sets;
* `LocalTime`;
* `LocalDate`;
* `LocalDateTime`.

## Supported features

JSON mapper supports:
* Identity retention - objects that have reference equality will also do so 
  after marshalling and unmarshalling (if enabled);
* Null values inclusion or exclusion;
* Unmarshalling unknown values handling policies:
  * Ignore - unknown values are replaces with nulls if possible;
  * Fail - unmarshalling procedure throws an exception if an unknown value 
    was encountered.

## Format

Objects are marshalled to and unmarshalled from a JSON. 
It is required for the marshalled object to be marked as `Exported`, as it
serves as a root object and a container for its fields' JSON models.

Fields or record components are written to JSON in an alphanumeric order.

If identity retention is enabled, additional service fields named `$ref` 
will be added with an ID of a reference shared by two or more objects as its value.