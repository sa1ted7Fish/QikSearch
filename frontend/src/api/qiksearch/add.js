import request from '@/utils/request'

/**
 * 新增题目到ES
 * @param {string} content 题目内容
 * @returns {Promise}
 */
export function addQuestion(content) {
    return request({
        url: '/qs/add',
        method: 'post',
        params: {
            questionContent: content
        }
    })
}
