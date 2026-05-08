import sys
import llm

if __name__ == "__main__":
    # 1. 接收 Java 传过来的命令行参数
    if len(sys.argv) < 3:
        print("错误：缺少食材或要求参数")
        sys.exit(1)

    food = sys.argv[1]
    demand = sys.argv[2]

    # 2. 执行预判拦截
    if not llm.is_relevant_question(food, demand):
        print("食材输入有误或要求与食谱无关")
        sys.exit(0)

    # 3. 直接调用工具库里封装好的核心函数
    try:
        result = llm.generate_recipe(food, demand)
        print(result)
    except Exception as e:
        print(f"调用 AI 发生异常: {str(e)}")
