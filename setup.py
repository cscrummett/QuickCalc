from setuptools import setup, find_packages

setup(
    name="QuickCalc",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[
        'numpy>=1.21.0'
    ],
    python_requires='>=3.8'
)
