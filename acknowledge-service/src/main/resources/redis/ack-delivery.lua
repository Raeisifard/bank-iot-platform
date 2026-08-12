local deliveryKey = KEYS[1]
local retryKey = KEYS[2]

local expectedClientId = ARGV[1]
local ackStatus = ARGV[2]
local ackAt = ARGV[3]

if redis.call('EXISTS', deliveryKey) == 0 then
    return 0
end

local currentStatus =
    redis.call('HGET', deliveryKey, 'status')

if currentStatus == 'ACKED' then
    return 2
end

local clientId =
    redis.call('HGET', deliveryKey, 'clientId')

if clientId ~= expectedClientId then
    return -1
end

redis.call(
    'HSET',
    deliveryKey,
    'status',
    'ACKED',
    'ackStatus',
    ackStatus,
    'ackAt',
    ackAt
)

redis.call(
    'ZREM',
    retryKey,
    redis.call('HGET', deliveryKey, 'messageId')
)

return 1

 --[[
 1 = ACK successfully processed
 2 = duplicate ACK
 0 = message does not exist
-1 = client does not own message
--]]