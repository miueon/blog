let target

export class Save {

  constructor(t, auth) {
    target = t
    this.auth = auth
  }

  async post(data, msg) {
    return axios.post(target.uri, data, {
      headers: {
        Authorization: this.auth
      }
    }).then(function (response) {
      target.msg = msg
      target.alert = false
      show_message(target)
      return response.data.payload
    })

  }

  check() {
    if (target.check_length()) {
      return true
    } else if (target.change) {
      this.promise = target.update().catch(function (err) {throw  err});
      return true
    }
    return false
  }

  async common(data, msg, callback) {
    try {
      if (!this.check()) {
        const result = await this.post(data, msg);
        console.log(result)
        callback(result);
      } else {
        await this.promise.then(callback);
      }
    } catch (err) {
      console.log(err)
      target.error_handler(err)
    }

  }

  async save(data, msg, jump_to) {
    await this.common(data, msg, () => {
      setTimeout(() => {
        window.location.href = jump_to
      }, 2000)
    })
  }

  async save_add(data, msg, jump_to) {
    await this.common(data, msg, () => {
      window.location.href = jump_to
    })
  }

  async save_edit(data, msg) {
    await this.common(data, msg, (response) => {
      if (!target.change) {
        target.change = true;
        target.id = Number.parseInt(response)
      }
    })
  }
}