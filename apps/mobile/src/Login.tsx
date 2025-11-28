import React, {useState} from 'react'
import { View, TextInput, Button, Text } from 'react-native'
import { login, setToken } from './api'

export default function Login({onLogin}:{onLogin:()=>void}){
  const [email,setEmail] = useState('demo@paddlers.test')
  const [msg,setMsg] = useState('')

  async function submit(){
    setMsg('')
    try{
      const res = await login(email)
      if(res.ok){
        const j = await res.json()
        await setToken(j.token)
        onLogin()
      } else {
        let body = null
        try { body = await res.json() } catch(e) { body = await res.text() }
        if(body && body.code){
          if(body.code === 'AUTH_INVALID_CREDENTIALS') setMsg('That email is not registered')
          else setMsg(body.message || JSON.stringify(body))
        } else if(typeof body === 'string') setMsg(body)
        else setMsg('Login failed')
      }
    } catch(e){ setMsg('Network error') }
  }

  return (
    <View style={{padding:16}}>
      <TextInput value={email} onChangeText={setEmail} placeholder="email" style={{borderWidth:1,padding:8,marginBottom:8}} />
      <Button title="Login" onPress={submit} />
      <Text style={{color:'red',marginTop:8}}>{msg}</Text>
    </View>
  )
}

