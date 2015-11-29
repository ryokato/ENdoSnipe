function global:Replace-Content
{
  Param([string]$filepath, [string]$rep1, [string]$rep2)
  if ( $(test-path $filepath) -ne $True )
  {
    Write-Error "存在しないパスです"
    return
  }
  $file_contents = $(Get-Content $filepath -encoding String) -replace $rep1, $rep2
  $file_contents | Out-File $filepath -encoding default
}


#タグ名称を設定する。
$tags = "6.0.1-release"

$WorkDir="build"

#tagを作成する。
git tag -a $tags -m 'build tag'
git push --tags

#作業ディレクトリを作成する。
Remove-Item -path $WorkDir -recurse
mkdir $WorkDir

#tagからファイルをエクスポートする。
git archive --format zip -o $WorkDir\export.zip $tags

#エクスポートしたファイルを展開する。
cd $WorkDir
$file=Convert-Path(".\export.zip")
$shell = New-Object -ComObject shell.application
$zip = $shell.NameSpace($file)
$dest =  $shell.NameSpace((Split-Path $file -Parent))

$dest.CopyHere($zip.Items()) 

# バージョンを更新する。
$tag_array = $tags -split "-"
$ver = $tag_array[0]
$build = $tag_array[1]

Replace-Content ENdoSnipe\build.bat "set VER=.+" "set VER=$ver"
Replace-Content ENdoSnipe\build.bat "set BUILD=.+" "set BUILD=$build"



# ビルドを実行する。
cd ENdoSnipe
.\build.bat