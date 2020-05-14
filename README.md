# D2EClassifier
Speech classifier of D2E AI block

## Run
- On Ubuntu or Mac

`./gradlew bootRun`

- On Windows

`./gradlew.bat bootRun`

![Run](docs/D2EClassifier.gif)

## Example

### yesno
`{"method":"yesno", "input":"맞는 것 같애"}`

### movements
`{"method":"movements", "input":"오른팔을 위로 올려봐"}`

### picknum
`{"method":"picknum", "input":"사십칠이야"}`

### choice
`{"method":"choice", "input":{"choice": ["오타와","토론토","몬트리올","밴쿠버","서울"], "answer": "정답은 오타와 인 것 같아"}}`
