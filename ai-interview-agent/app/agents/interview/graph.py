from langgraph.graph import END, START, StateGraph

from app.agents.interview.nodes import initialize_interview
from app.agents.interview.state import InterviewState

builder = StateGraph(InterviewState)
builder.add_node("initialize_interview", initialize_interview)
builder.add_edge(START, "initialize_interview")
builder.add_edge("initialize_interview", END)

interview_graph = builder.compile()
