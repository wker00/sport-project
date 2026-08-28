import { ElMessage } from 'element-plus'

export function toast(message, type = 'success') {
    ElMessage({
        message,
        type,
        showClose: false,
        duration: 1500,
    })
}
