import request from '@/utils/request'

/**
 * 搜索接口
 * @param query 参数对象
 * @returns {AxiosPromise}
 */
export function getList(query) {
    return request({
        url: '/qs-dev/search',
        method: 'get',
        params: query
    })
}
