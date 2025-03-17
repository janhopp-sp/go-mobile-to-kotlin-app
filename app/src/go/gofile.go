package logging

type LogCallback interface {
    Log(message string)
}

func AddLogCallback(logCallback LogCallback) {
	logCallback.Log("Hello from Go!")
}

func Hello(name string) string {
	return "Hello from " + name + "!"
}
