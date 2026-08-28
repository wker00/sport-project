import JSEncrypt from 'jsencrypt'
import axios from 'axios'

let publicKey = ''

export async function initRsaKey() {
  try {
    const res = await axios.get('/api/public/key')
    publicKey = res.data?.message || ''
  } catch {
    console.error('获取RSA公钥失败')
  }
}

export function encryptPassword(password) {
  if (!publicKey) throw new Error('RSA公钥未加载，请刷新页面重试')
  const encrypt = new JSEncrypt()
  encrypt.setPublicKey(publicKey)
  const result = encrypt.encrypt(password)
  if (!result) throw new Error('密码加密失败')
  return result
}
