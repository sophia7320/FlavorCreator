import requests
import json
import os

def ask_llm(messages):
    url = ""

    payload = {
        "model": "",
        "messages": messages,
    }
    headers = {
        "Authorization": "Bearer sk-vigcsnmtiayvgrwtudutzqvnqzrluppqppgrrrzryynvkmov",
        "Content-Type": "application/json"
    }

    response = requests.post(url, json=payload, headers=headers)
    data = response.json()
    result_text = data["choices"][0]["message"]["content"]
    return result_text

def call_llm(messages):
    url = ""

    payload = {
        "model": "",
        "messages": messages,
    }
    headers = {
        "Authorization": "Bearer sk-vigcsnmtiayvgrwtudutzqvnqzrluppqppgrrrzryynvkmov",
        "Content-Type": "application/json"
    }

    response = requests.post(url, json=payload, headers=headers)
    data = response.json()
    result_text = data["choices"]["message"]["content"]
    return result_text

def is_relevant_question(user_input1:str, user_input2: str) -> bool:
    #用一个轻量LLM
    check_prompt = f"""
    判断{food}里面是否完全是食材。，{demand}里面是否完全是与食谱有关的要求。
    只能回答YES或NO。
    只有两个都正确时回答YES，否则回答NO。
    """
    messages = [{"role": "user", "content": check_prompt}]
    result = call_llm(messages)
    return result.strip().upper() == "YES"


def generate_recipe(food: str, demand: str) -> str:
    prompt = f"""
你是一个专业的食谱问答助手，只回答与flavor creator相关的问题，你的知识范围只包括提供食谱。
以下给可用食材（已包括调料）和食谱要求，请提供几份食谱。
注意：
1.必须严格考虑食材的相冲规则
2.如果用户的问题与产品无关，礼貌拒绝并引导回正题
3.不讨论政治、娱乐、编程、其他竞品等无关话题
4.你的回答不能超过300字
5.你只能使用已有的食材。如果现有食材不能满足食谱所需，你可以自创食谱，但需要标明该食谱为ai自创仅供参考
可用食材：{food}
食谱要求：{demand}
如果可用食材不足或者可用食材中出现了非食材，请不要提供食谱，只回答“食材输入有误”。
如果食谱要求与食谱无关，请只回答“食材要求有误”。
"""
    messages = [
        {"role": "system", "content": prompt},
        {"role": "user", "content": food},
        {"role": "user", "content": demand},
    ]
    return ask_llm(messages)

if __name__ == "__main__":
    food = input("你想使用的的食材（包含调料）是：")
    demand = input("你的要求是：")
    result = generate_recipe(food, demand)
    print(result)
