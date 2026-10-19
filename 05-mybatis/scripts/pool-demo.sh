#!/usr/bin/env bash
# 课后进阶 2：复现连接池耗尽
#
# 用法：
#   ./scripts/pool-demo.sh [并发数] [接口占用连接的时间(毫秒)]
#
# 前置：
#   1) 应用已启动（默认 http://localhost:8005）
#   2) 先把 application.yml 里的 maximum-pool-size 调成 1、minimum-idle 调成 1，重启应用；
#      实验完再调回 10 / 5 重启，用同一个脚本跑一遍对照。
#
# 现象：
#   池 = 1 时，第一个请求占用连接 5 秒，其余请求 3 秒（connection-timeout）后抛
#   SQLTransientConnectionException: ... Connection is not available, request timed out after 3000ms
#   池 = 10 时，所有请求都能拿到连接，全部成功。

set -u

CONCURRENCY=${1:-5}
HOLD_MS=${2:-5000}
BASE_URL=${BASE_URL:-http://localhost:8005}
TMP_DIR=$(mktemp -d)

echo "并发数 = ${CONCURRENCY}，每个请求占用连接 ${HOLD_MS} ms，目标 ${BASE_URL}/users/slow"
echo "------------------------------------------"

start=$(date +%s)
for i in $(seq 1 "$CONCURRENCY"); do
  (
    # 末尾三个值：HTTP 状态码、响应体、耗时(秒)
    body_file="${TMP_DIR}/body_${i}.txt"
    code=$(curl -s -m 30 -o "$body_file" -w "%{http_code}" "${BASE_URL}/users/slow?ms=${HOLD_MS}")
    end=$(date +%s)
    echo "${code}|$(cat "$body_file")|$((end - start))" > "${TMP_DIR}/result_${i}.txt"
  ) &
done
wait

for i in $(seq 1 "$CONCURRENCY"); do
  result=$(cat "${TMP_DIR}/result_${i}.txt" 2>/dev/null || echo "???||0")
  code=${result%%|*}
  rest=${result#*|}
  body=${rest%%|*}
  cost=${rest##*|}
  echo "请求 ${i}: HTTP ${code}, 耗时约 ${cost}s, 响应 = ${body}"
done

rm -rf "$TMP_DIR"
