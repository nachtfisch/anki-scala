# anki-scala

The project is based on anki and provides a simple REST API to play a bit with the algorithm. 

The actual SRS algorithm was ported from Anki.

The current REST API endpoints for the SRS:

    GET /api/study/reviews  // gets all open reviews
    POST /api/study/reviews // adds a new review, takes a json object with param factId to reference a foreign id
    GET /api/study/reviews/{review-id} // retrieve a review object
    POST /api/study/reviews/{review-id} // perform a review takes json ReviewRequest(ease:Int, reviewTime:DateTime = DateTime.now())
    
The current REST API endpoints for deck card creation (still draft). This was more intended to get a quick look into how 
Anki does it's rendering.

    POST /api/cards/render takes as parameter a render request
      case class Template(front: String, back: String)
      case class ModelField(name: String, value: String)
      case class ModelFact(fields: List[ModelField])
      case class RenderRequest(templates: List[Template], facts: List[ModelFact])           