package de.nachtfische.ankimodel

case class FieldDescription(name:String, ord:Int)
case class TemplateDescription(name:String, qfmt:String, afmt:String)
case class CardModel(id: String, name: String, flds: List[FieldDescription], tags:List[String], tmpls: List[TemplateDescription])

case class Field(name:String, value:String)

case class Fact(modelId:CardModel, fields:List[String])