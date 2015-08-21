package de.nachtfische.ankimodel

object CardRendering {
    case class Template(front: String, back: String)
    case class ModelField(name: String, value: String)
    case class ModelFact(fields: List[ModelField])
    case class RenderRequest(templates: List[Template], facts: List[ModelFact])
}
