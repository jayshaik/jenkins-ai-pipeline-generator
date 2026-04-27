from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from mangum import Mangum
import os
from openai import OpenAI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# Enable CORS so frontend can call backend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # allow frontend on port 3000
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load API key securely
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

class PipelineRequest(BaseModel):
    language: str
    build_tool: str
    deployment: str

@app.get("/")
def health_check():
    return {"message": "Jenkins AI Generator API is running"}

@app.post("/generate")
def generate_pipeline(req: PipelineRequest):
    try:
        prompt = f"""
        Generate a production-ready Jenkins Declarative Pipeline (Jenkinsfile).

        Requirements:
        - Language: {req.language}
        - Build Tool: {req.build_tool}
        - Deployment: {req.deployment}

        Include stages:
        - Checkout
        - Build
        - Test
        - Docker build (if applicable)
        - Deployment

        Best practices:
        - Use proper syntax
        - Add comments
        - Handle failures where possible

        Return ONLY the Jenkinsfile code.
        """

        response = client.chat.completions.create(
            model="gpt-4.1",
            messages=[
                {"role": "system", "content": "You are a DevOps expert."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.3
        )

        return {
            "jenkinsfile": response.choices[0].message.content
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# Lambda handler
handler = Mangum(app)