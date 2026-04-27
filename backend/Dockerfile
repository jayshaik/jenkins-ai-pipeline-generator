FROM public.ecr.aws/lambda/python:3.9

# Copy requirements
COPY requirements.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy app code
COPY api.py .

# Set the Lambda handler
CMD ["api.handler"]